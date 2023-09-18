package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.Field
import org.joda.time.DateTime

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime

//@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ZAW-1317")
/**
 * This function creates Release RFC on SM using Rest API call
 */
def mainMethod() {
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def cFActualStart = CommonUtil.getCustomFieldValue(issue, 14601).toString()
    def cFActualEnd = CommonUtil.getCustomFieldValue(issue, 14602).toString()
    def cfIssues = CommonUtil.getCustomFieldValue(issue, 14603).toString()
    def cfIssuesDetails = CommonUtil.getCustomFieldValue(issue, 14604).toString()
    def cfFailureReason = CommonUtil.getCustomFieldValue(issue, 14605).toString()
    def issueResolution = issue.resolution?.name

    def deploymentProgress = issueResolution == 'Pass' ? 'Implementation Completed' : 'Implementation Failed'
    def actStartFormatted =  dateTimeFormat(cFActualStart)  //Date.parse('yyyy-MM-dd HH:mm:ss', cFActualStart).format("yyyy-MM-dd'T'HH:mm:ss")
    def actEndFormatted = dateTimeFormat(cFActualEnd)      //Date.parse('yyyy-MM-dd HH:mm:ss', cFActualEnd).format("yyyy-MM-dd'T'HH:mm:ss")

    def closeChange = putChange(actStart: actStartFormatted,
            actEnd: actEndFormatted,
            issues: cfIssues,
            issueDetails: cfIssuesDetails,
            ProgressStatus: deploymentProgress,
            failureReason: cfFailureReason,
            cFRFCNumber: cFRFCNumber, issue)

        postChangeActivity(cFRFCNumber, issue)
}

/**
 * This function creates Release RFC on SM using Rest API call
 */
def putChange(Map m,def issue) {
    def rfcData = [
        Change: [
            ActualImplementationStart : m.actStart,
            ActualImplementationEnd : m.actEnd,
            Issues : m.issues,
            IssueDetails :m.issueDetails,
            ProgressStatus :m.ProgressStatus,
            "elm.jira.status"          : "Done",
            "elm.chm.failed.causes" : m.failureReason,
            header                     : [
                Category    : "Elm Release",
                Phase       : "Change Implementation",
                Status      : "close"
            ]
        ]
    ]

    def resp = HttpRestUtil.SMPut("/SM/9/rest/changes/${m.cFRFCNumber}",new Gson().toJson(rfcData))
    //CommonUtil.addCommentToIssue(Globals.botUser,resp?.body?.object?.Messages,issue)
    return (resp.body?.object)
}

/**
 * This function creates Release RFC on SM using Rest API call
 */
def postChangeActivity(String rfcNumber,def issue) {
    def comment = ComponentAccessor.commentManager.getLastComment(issue)?.body
    if (comment) {
        def activityRFC = [
            activitychange: [
                number     : rfcNumber,
                type       : "Closed by Jira",
                operator   : "elmjirauser",
                description: comment
            ]
        ]
        def resp = HttpRestUtil.SMPost('/SM/9/rest/activitychange', new Gson().toJson(activityRFC))
    }
}

if(issue.projectObject.key != 'PERP') {
    mainMethod()
}

/**
 * get the time in between status Awaiting Business approval
 */
static def dateTimeFormat(def dateValue){
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    Date date = format.parse(dateValue)
    def strDate =  new DateTime(date).minusHours(3).toString()
    return   strDate.substring(0, strDate.length() - 10)
}