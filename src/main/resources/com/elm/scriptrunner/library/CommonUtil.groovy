package com.elm.scriptrunner.library

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.workflow.TransitionOptions
import groovy.json.JsonSlurper
import groovy.transform.Field
import groovy.util.logging.Log4j
import org.apache.log4j.Logger

import java.text.SimpleDateFormat

@Log4j
class CommonUtil {

    static def getCustomFieldObject(Long fieldId) {
        ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)
    }

    static def getCustomFieldValue(issue, Long fieldId) {
        ComponentAccessor.customFieldManager.getCustomFieldObject(fieldId)?.getValue(issue)
    }

    static def getProjectObjByName(String name) {
        ComponentAccessor.projectManager.getProjectObjByName(name)
    }

    static def getProjectObjByIssue(issue) {
        ComponentAccessor.projectManager.getProjectObjByName(issue.getProjectObject().getName().toString())
    }

    static def getProjectRole(issue, Long Field){
        ComponentAccessor.getComponent(ProjectRoleManager).getProjectRole(getCustomFieldValue(issue,Field).toString())}

    static ApplicationUser executeScriptWithAdmin() {
        def util = ComponentAccessor.getUserUtil()
        def adminsGroup = ComponentAccessor.getGroupManager().getGroup("atlassian_admin_users")
        ApplicationUser executingAdmin = ComponentAccessor.getUserManager().getUserByName("mmojahed")
        ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(executingAdmin)
        return (executingAdmin)
    }

    static ApplicationUser executeScriptWithAdmin( String username ) {
        def util = ComponentAccessor.getUserUtil()
        def adminsGroup = ComponentAccessor.getGroupManager().getGroup("atlassian_admin_users")
        ApplicationUser executingAdmin = ComponentAccessor.getUserManager().getUserByName(username)
        ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(executingAdmin)
        return (executingAdmin)
    }

    static def getProjectName(issue) {
        getProjectObjByIssue(issue).name
    }

    static def getProjectKey(issue) {
        getProjectObjByIssue(issue).key
    }

    static def findIssues(String jqlSearch, ApplicationUser searcher) {
        def searchService = ComponentAccessor.getComponent(SearchService)
        def issueManager = ComponentAccessor.getIssueManager()
        SearchService.ParseResult parseResult = searchService.parseQuery(searcher, jqlSearch)
        def searchResult = searchService.search(searcher, parseResult.getQuery(), PagerFilter.getUnlimitedFilter())
        def results = searchResult.results.collect {issueManager.getIssueObject(it.id)}
        return results
    }

    static def transitionIssue(ApplicationUser botUser, long issueId, int ACTION_ID){
        def issueService = ComponentAccessor.getIssueService()
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.setSkipScreenCheck(true)

        def transitionOptions= new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()

        def transitionValidationResult =
            issueService.validateTransition(botUser, issueId, ACTION_ID, issueInputParameters, transitionOptions)

        if (transitionValidationResult.isValid()) {
            return issueService.transition(botUser, transitionValidationResult).getIssue()
        }
         else {
            return transitionValidationResult.errorCollection
        }
        //log.error "Transition of issue ${issue.key} user. " + transitionValidationResult.errorCollection
    }

    static def transitionIssueWithResolution(ApplicationUser botUser, long issueId, int ACTION_ID , String resolutionId){
        def issueService = ComponentAccessor.getIssueService()
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.setResolutionId(resolutionId)
        issueInputParameters.setSkipScreenCheck(true)

        def transitionOptions= new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()

        def transitionValidationResult =
            issueService.validateTransition(botUser, issueId, ACTION_ID, issueInputParameters, transitionOptions)

        if (transitionValidationResult.isValid()) {
            return issueService.transition(botUser, transitionValidationResult).getIssue()
        }
        else {
            return transitionValidationResult.errorCollection
        }
        //log.error "Transition of issue ${issue.key} user. " + transitionValidationResult.errorCollection
    }

    static boolean isCdx(String project) {

        def JJPKeyName = getCustomFieldObject(13608).name.toString()
        ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")
        def jql = "'${JJPKeyName}' ~  '${project}'"
        List mappingResults = findIssues(jql, botUser) as List
        log.debug("Mapping Results : ${mappingResults}")
        log.debug("Is Cdx: ${mappingResults.isEmpty()}")
        return(!mappingResults.isEmpty())
    }

    static def setLabel(long issueId){
        def labelManager = ComponentAccessor.getComponent(LabelManager)
        def labels = labelManager.getLabels(issueId).collect{it.getLabel()}
        labels += 'ToBeDeleted'
        def issueObject = ComponentAccessor.getIssueManager().getIssueObject(issueId)
        labelManager.setLabels(Globals.botUser,issueId,labels.toSet(),false,false)
    }

    static def addCommentToIssue(def user, def comnts,def issue) {
        CommentManager commentManager = ComponentAccessor.getCommentManager()
        def properties = [(Globals.SD_PUBLIC_COMMENT): new JSONObject(["internal": true])]
        commentManager.create(issue, user, comnts, null, null, new Date(), properties, true)
    }

    static def getInsightCField(def issue , def insightObjCF, def insightObjAttrObj){
        try {
            def serviceCF = getCustomFieldValue(issue, insightObjCF)
            def attrValue = []
            def objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
                findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade")
            def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass)
            serviceCF.each {
                def myAttrValue = objectFacade.loadObjectAttributeBean(it.id, insightObjAttrObj).getObjectAttributeValueBeans()[0].getValue()
                attrValue.add(myAttrValue)
            }
            return attrValue

        }catch(Exception e){
            log.warn("Object not Found")
        }
    }

    static def getInsightCFieldObject(def issue , def iql,def objectSchemaId){
        def objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade")
        def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass)
        objectFacade.validateIQL(iql)
        def objects = objectFacade.findObjectsByIQLAndSchema(objectSchemaId, iql)
        return objects
    }

    static def getInsightAtrributeValueSpecificObject(def iql,def objectSchemaId,def insightObjAttrObj){
        def attrValue = []
        def IQLFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade")
        def objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade")
        def iQLFacade = ComponentAccessor.getOSGiComponentInstanceOfType(IQLFacadeClass)
        def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass)
        iQLFacade.validateIQL(iql)
        def object = iQLFacade.findObjectsByIQLAndSchema(objectSchemaId, iql)
        def myAttrValue = objectFacade.loadObjectAttributeBean(object.first().id, insightObjAttrObj).getObjectAttributeValueBeans()[0].getValue()
        attrValue.add(myAttrValue)
        return attrValue
    }

    static def getInsightCFValueSpecificAttribute(def insightObjKey, def insightObjAttrObj) {
        def attrValue = []
        def objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade")
        def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass)
        try{
            def myAttrValue = objectFacade.loadObjectAttributeBean(insightObjKey, insightObjAttrObj).getObjectAttributeValueBeans()[0].getValue()
            attrValue.add(myAttrValue)
            return attrValue
        }
        catch (Exception e) {
            log.warn('object not found')
            return 'null'
        }
    }

    static def getInsightObjectByAttributeValue(def iql,def objectSchemaId){
        def IQLFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade")
        def objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().
            findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade")
        def iQLFacade = ComponentAccessor.getOSGiComponentInstanceOfType(IQLFacadeClass)
        def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass)
        iQLFacade.validateIQL(iql)
        def object = iQLFacade.findObjectsByIQLAndSchema(objectSchemaId, iql)
        if (object != null && object.size() > 0){
            def insightObject = object[0]
            return insightObject
        }
    }
//    static def RemoveReleaseNotePageRestriction(def issue){
//        //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
//        def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(), "UTF-8")
//        def pKey = getProjectKey(issue)
//        def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
//        def getPageInfo = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/search?cql=space=${wikiKey}%20and%20title='${originalPageTitle}'")
//        def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
//        def removePageRestriction = "?pageId=${pageId}&user=mmojahed"
//        def wikiBaseURL = "/rest/keplerrominfo/refapp/latest/webhooks/removePageRestriction/run$removePageRestriction"
//        DoRequestCall.wikiPermissionRestCall(wikiBaseURL)
//    }
    static def getOptionValue (def issue ,Long fieldId ,def newOptionValue) {
        def customFieldObject = getCustomFieldObject(fieldId)
        def optionsManager = ComponentAccessor.getOptionsManager()

        def config = customFieldObject.getRelevantConfig(issue)
        def options = optionsManager.getOptions(config)
        def optionToSelect = options.find {
            it.value == newOptionValue
        }
        return optionToSelect
    }

    static def dateFormat(def issue , Long customfieldId){
        def cFValue = getCustomFieldValue(issue, customfieldId)
        log.warn(cFValue)
        if(cFValue) {
            return (new SimpleDateFormat("dd/MM/yy HH:mm").format(cFValue))
        }
        else {return ("")}
    }
}