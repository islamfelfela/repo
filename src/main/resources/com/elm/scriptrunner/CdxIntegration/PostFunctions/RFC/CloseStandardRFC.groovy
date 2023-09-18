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

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ZAW-1317")

issue.projectObject.projectCategory.name
def mainMethod() {
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def cFActualStart = LocalDateTime.now().minusHours(72).toString()
    def cFActualEnd = LocalDateTime.now().toString()
    def cfIssues = CommonUtil.getCustomFieldValue(issue, 14603).toString()
    def cfIssuesDetails = CommonUtil.getCustomFieldValue(issue, 14604).toString()
//    def cfFailureReason = CommonUtil.getCustomFieldValue(issue, 14605).toString()
    def issueResolution = issue.resolution?.name

    def deploymentProgress = issueResolution == 'Completed' ? 'Implementation Completed' : 'Implementation Failed'

//    def actStartFormatted =  dateTimeFormat(LocalDateTime.now().toString())  //Date.parse('yyyy-MM-dd HH:mm:ss', cFActualStart).format("yyyy-MM-dd'T'HH:mm:ss")
//    def actEndFormatted = dateTimeFormat(LocalDateTime.now().toString())      //Date.parse('yyyy-MM-dd HH:mm:ss', cFActualEnd).format("yyyy-MM-dd'T'HH:mm:ss")

    def closeChange = putChange(actStart: cFActualStart,
            actEnd: cFActualEnd,
            issues: cfIssues,
            issueDetails: cfIssuesDetails,
            ProgressStatus: deploymentProgress,
            cFRFCNumber: cFRFCNumber, issue)

        postChangeActivity(cFRFCNumber, issue)
}

def putChange(Map m,def issue) {
    def rfcData = [
        Change: [
            ActualImplementationStart : m.actStart,
            ActualImplementationEnd : m.actEnd,
            Issues : m.issues,
            IssueDetails :m.issueDetails,
            ProgressStatus :m.ProgressStatus
        ]
    ]

    def resp = HttpRestUtil.SMPut("/SM/9/rest/changes/${m.cFRFCNumber}",new Gson().toJson(rfcData))
    //CommonUtil.addCommentToIssue(Globals.botUser,resp?.body?.object?.Messages,issue)
    return (resp.body?.object)
}

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

mainMethod()


static def dateTimeFormat(def dateValue){
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    Date date = format.parse(dateValue)
    def strDate =  new DateTime(date).minusHours(3).toString()
    return   strDate.substring(0, strDate.length() - 10)
}