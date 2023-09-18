package com.elm.scriptrunner.CdxIntegration.Validators

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.json.JSONObject
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.PluginProperty
import com.google.gson.Gson
import com.opensymphony.workflow.InvalidInputException
import groovy.transform.Field

    log.warn("Change Request ID:  "+issue)


    String JJSubComponentName= CommonUtil.getCustomFieldObject(13609).name
    def JJSubComponentValue = CommonUtil.getCustomFieldValue(issue,13609)
    String subComponentJQLString=JJSubComponentValue.toString()!="null"? "~ '${JJSubComponentValue.toString()}'":" is empty"
    String JJPKeyName = CommonUtil.getCustomFieldObject(13608).name

    //#################Project Change Values##########################################
    def componentManager = ComponentAccessor.projectComponentManager
    def allIssueComponents = componentManager.findComponentsByIssue(issue)
    def fixVersion = issue.getFixVersions().get(0).toString()
    def changeComponent = allIssueComponents ? allIssueComponents.getAt(0).name : "n/a"

    //#################Search Issues mapping###########################################
    def jql = "'${JJPKeyName}' ~  '${issue.projectObject.key.toString()}' " +
            " and component='${changeComponent.toString()}' and '${JJSubComponentName}'" +
            " ${subComponentJQLString} "
log.warn("Request JQL Query:  "+jql)

    def mappingResults = CommonUtil.findIssues(jql, Globals.botUser)
    if(mappingResults) {
        def mappingIssueKey = ComponentAccessor.getIssueManager().getIssueByCurrentKey(mappingResults.get(0).toString())
        def jenkingMapingURL = CommonUtil.getCustomFieldValue(mappingIssueKey,13603).toString().split("/job/")
        def urlEncoded = URLEncoder.encode(jenkingMapingURL.last(), "UTF-8")
        def uriPath = "/job/${jenkingMapingURL.first()}/job/${urlEncoded}/buildWithParameters"
        def buildData = [version:fixVersion,jiraIssueKey:issue.key.toString()]
        log.warn("fix Version: " + fixVersion)
        log.warn("Jenkin Call URL: "+ Constants.JenkinsUrl+uriPath)

        def jenkingCall = HttpRestUtil.JPost(uriPath,buildData)
        def commentsString =
                "\nExecuted By: ${Globals.botUser}" +
                        "\nComponent: ${changeComponent}" +
                        "\n Sub Component: "

        if (jenkingCall.status != 201) {
            // addCommentToIssue([botUser], commentsString, issue)
            throw new InvalidInputException("Some error occured while calling Jenkin, please try again, Status Code: ${jenkingCall.status}")
        } else {
             updateCustomField()
            // addCommentToIssue(botUser, commentsString, issue)
        }

    }else {
        log.warn("No mapping results found")
        throw new InvalidInputException("Some error occurred while calling Jenkin, Please contact: techsupport@elm.sa ")
    }



def addCommentToIssue( def comnts, def issue) {
    CommentManager commentManager = ComponentAccessor.getCommentManager()
    def properties = [(Globals.SD_PUBLIC_COMMENT): new JSONObject(["internal": true])]
    commentManager.create(issue, Globals.botUser, comnts, null, null, new Date(), properties, true)
}

def updateCustomField(){
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def issueManafer = ComponentAccessor.getIssueManager()
    def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    def cf = customFieldManager.getCustomFieldObject(13700 as long)
    issue.setCustomFieldValue(cf,"Jenkins in progress")
    issueManafer.updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}