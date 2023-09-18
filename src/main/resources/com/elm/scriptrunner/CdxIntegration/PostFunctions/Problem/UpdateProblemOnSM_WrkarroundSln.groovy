package com.elm.scriptrunner.CdxIntegration.PostFunctions.Problem

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson

/**
 * a post function that updates the problem status to Workaround Provided
 */

def mainMethod() {
    def issueStatus = "Workaround Provided"
    def cFRootCause = CommonUtil.getCustomFieldValue(issue, 12002).toString()
    def cFProblemID = CommonUtil.getCustomFieldValue(issue, 11101).toString()
    def cFWSolution = CommonUtil.getCustomFieldValue(issue, 12003).toString()

    def problemData = [
        Problem: [
            Status         : issueStatus,
            RootCause      : cFRootCause,
            elmsolutiontype: "Technical",
            Workaround     : cFWSolution
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
    log.warn(prblmCommentRestCall)
}
mainMethod()