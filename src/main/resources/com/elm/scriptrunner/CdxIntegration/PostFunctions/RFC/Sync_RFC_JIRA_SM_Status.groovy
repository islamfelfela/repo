package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil

import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("WSJ-919")

def mainMethod() {
   //def jqlQuery = "type = Change AND status in (\"Awaiting Release\", Scheduling) AND \"Change number\" is not EMPTY AND \"Deployment Type\" is EMPTY"
   //def issues = CommonUtil.findIssues(jqlQuery, Globals.botUser)

    //issues.each { issue ->
        def cFChangeID = CommonUtil.getCustomFieldValue(issue, 11311).toString()
        def changeRestCall = HttpRestUtil.SMGet('/SM/9/rest/changes/' + cFChangeID)
        def SMChangeStatus = changeRestCall.body?.object?.Change?.header?.Phase?.toString()
        log.warn('issueKey: ' + issue.key + ', Status: ' + issue.status.name + ', ' + SMChangeStatus)
        def SMProgressStatus = changeRestCall.body?.object
        if (SMChangeStatus == 'Change Implementation' && issue.status.name != 'Awaiting Release') {
            CommonUtil.transitionIssue(Globals.botUser, issue.id, 551)
        }
        else if (SMChangeStatus == 'Change Closure' && issue.status.name != 'Done') {
            if (SMProgressStatus.Change.has("ProgressStatus")) {
                if (SMProgressStatus.Change?.ProgressStatus?.toString() == 'Implementation Completed') {
                    CommonUtil.transitionIssueWithResolution(Globals.botUser, issue.id, 541, '11201')
                } else if (SMProgressStatus.Change?.ProgressStatus?.toString() == 'Implementation Failed') {
                    CommonUtil.transitionIssueWithResolution(Globals.botUser, issue.id, 541, '11202')
                }
            }else {
                CommonUtil.transitionIssueWithResolution(Globals.botUser, issue.id, 541, '10800')
            }
        }
        else if (issue.status.name == "Scheduling"){
            if (SMChangeStatus == "Change Logging") {
                CommonUtil.transitionIssue(Globals.botUser, issue.id, 311)
            }
        }
    }


mainMethod()
