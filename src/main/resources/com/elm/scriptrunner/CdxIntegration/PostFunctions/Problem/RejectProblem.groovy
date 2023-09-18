package com.elm.scriptrunner.CdxIntegration.PostFunctions.Problem

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson

/**
 * a post function that updates the problem status to Rejected by Development
 */
def mainMethod() {
    def issueStatus = "Rejected by Development"
    def cFRootCauseDes = CommonUtil.getCustomFieldValue(issue,15503).toString()
    def cFProblemID = CommonUtil.getCustomFieldValue(issue,11101).toString()

    def problemData = [
        Problem: [
            Status                   : issueStatus,
            JiraRejectionReason      : cFRootCauseDes,
        ]
    ]
    def problemActivtyData = [
        activityproblem: [
            number     : cFProblemID,
            type       : "Update from Jira",
            operator   : "elmjirauser",
            description: issueStatus
        ]
    ]

    def prblmRestCall = HttpRestUtil.SMPut('/SM/9/rest/problems/' + cFProblemID,new Gson().toJson(problemData))
    log.warn(prblmRestCall)
    def prblmCommentRestCall = HttpRestUtil.SMPost('/SM/9/rest/activityproblem',new Gson().toJson(problemActivtyData))
    log.warn(prblmCommentRestCall?.body?.object)
}

mainMethod()
