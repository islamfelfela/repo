package com.elm.scriptrunner.CdxIntegration.PostFunctions.Problem

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
/**
 * a post function that updates the problem status to Permanent Solution Provided
 */

def mainMethod() {
    def issueStatus = "Permanent Solution Provided"
    def cFRootCause = CommonUtil.getCustomFieldValue(issue,12002).toString()
    def cFProblemID = CommonUtil.getCustomFieldValue(issue,11101).toString()
    def cFPSolution = CommonUtil.getCustomFieldValue(issue,12004).toString()
    def enhancedSolution = 'Resolution : ' + issue.resolution.name +' --------\n' + CommonUtil.getCustomFieldValue(issue,12004).toString()

    def problemData = [
        Problem: [
            Status         : issueStatus,
            RootCause      : cFRootCause,
            Solution       : enhancedSolution,
            elmsolutiontype: "Technical"
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
