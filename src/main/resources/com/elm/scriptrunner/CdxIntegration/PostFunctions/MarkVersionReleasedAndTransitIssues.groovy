package com.elm.scriptrunner.CdxIntegration.PostFunctions

import com.atlassian.jira.bc.project.version.VersionService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonSlurper

/**Update customFieldValue with UpdateIssue**/
def mainMethod() {
    //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
    def cfClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()
    def jqlSearch = "project='${issue.getProjectObject().name}' and fixVersion ='${issue.fixVersions.last()}' and issuetype in (Story,Bug,Technical-Task,'Security Bug',Enhancement,'Pain Point')"
    def deliveredChangeIssues = CommonUtil.findIssues(jqlSearch, Globals.botUser)
    log.warn(issue.key)
    if (cfClassification == 'Production' && issue.resolution.name == 'Pass') {
        markVersionAsReleased(Globals.botUser, issue)
        deliveredChangeIssues.each { it ->
            if (it.issueType.name == 'Bug') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 371) //  241
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Enhancement') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 411) //  241
                log.warn(it.key + it.status.name)
            }else if (it.issueType.name == 'Story') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 251)
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Technical-Task') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 811)
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Security Bug' && it.status.name.toLowerCase() == 'awaiting release') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 221)
                log.warn(it.key + it.status.name)
            }else if (it.issueType.name == 'Pain Point') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 431)
                log.warn(it.key + it.status.name)
            }
        }
    } else if (issue.resolution.name in ['Declined', 'Fail']) {
        deliveredChangeIssues.each {
            def labelManager = ComponentAccessor.getComponent(LabelManager)
            def labels = labelManager.getLabels(it.id).collect { it.getLabel() }
            labels += 'waitingForNextChange'
            labelManager.setLabels(Globals.botUser, it.id, labels.toSet(), false, false)
        }

        def jqlSearchWaitingRelease = "project='${issue.getProjectObject().name}' labels = waitingForNextChange "
        def waitingIssuesForNextChange = CommonUtil.findIssues(jqlSearchWaitingRelease, Globals.botUser)

        waitingIssuesForNextChange.each {
            if (it.issueType.name == 'Bug') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 371)
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Enhancement') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 411)
                log.warn(it.key + it.status.name)
            }else if (it.issueType.name == 'Story') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 251)
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Technical-Task') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 811)
                log.warn(it.key + it.status.name)
            } else if (it.issueType.name == 'Security Bug' && it.status.name.toLowerCase() == 'awaiting release') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 221)
                log.warn(it.key + it.status.name)
            }else if (it.issueType.name == 'Pain Point') {
                CommonUtil.transitionIssue(Globals.botUser, it.id, 431)
                log.warn(it.key + it.status.name)
            }
        }

        RemoveReleaseNotePageRestriction(issue)
    }
}

def markVersionAsReleased(ApplicationUser user, def issue){
    def versionService = ComponentAccessor.getComponent(VersionService)
    def version = issue.getFixVersions().first()
    def releaseVersionValidationResult = versionService.validateReleaseVersion(Globals.botUser, version, issue.resolutionDate)
    if (releaseVersionValidationResult.isValid()) {
        versionService.releaseVersion(releaseVersionValidationResult)
        log.warn(version.name)
    } else {
        log.warn(releaseVersionValidationResult.errorCollection)
    }
}

boolean isCDx(def issue){
    def changeProject = CommonUtil.getProjectObjByIssue(issue).key.toString()
    def changeComponent = issue.getComponents().isEmpty() ? "n/a" : issue.getComponents().first().name
    def JJPKeyName = CommonUtil.getCustomFieldObject(13608).name.toString()
    def jql = "'${JJPKeyName}' ~  '${changeProject}' and component='${changeComponent.toString()}'"
    List mappingResults = CommonUtil.findIssues(jql, Globals.botUser) as List
    return mappingResults.isEmpty()
}


def RemoveReleaseNotePageRestriction(def issue) {
         CommonUtil.executeScriptWithAdmin()
         //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
         def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(), "UTF-8")
         def pKey = CommonUtil.getProjectKey(issue)
         def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
         def getPageInfo = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/search?cql=space=${wikiKey}%20and%20title='${originalPageTitle}'")
         def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
         def removePageRestriction = "?pageId=${pageId}&user=mmojahed"
         def wikiBaseURL = "/rest/keplerrominfo/refapp/latest/webhooks/removePageRestriction/run$removePageRestriction"
         DoRequestCall.wikiPermissionRestCall(wikiBaseURL)
     }


mainMethod()