package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.Field
import groovyx.net.http.ContentType
import kong.unirest.HttpResponse
import kong.unirest.Unirest

import java.time.LocalDateTime

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ZAW-1297")

issue.fixVersions.size() > 0

def mainMethod() {
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def cFActualStart = CommonUtil.getCustomFieldValue(issue, 14601).toString()
    def cFActualEnd = CommonUtil.getCustomFieldValue(issue, 14602).toString()
    def cfIssues = CommonUtil.getCustomFieldValue(issue, 14601).toString()
    def cfIssuesDetails = CommonUtil.getCustomFieldValue(issue, 14602).toString()
    def cfFailureReason = CommonUtil.getCustomFieldValue(issue, 14605).toString()
    def issueResolution = issue.resolution.name
    def deploymentProgress = issueResolution == 'Pass' ? 'Implementation Completed' : 'Implementation Failed'

//    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s", ""), "UTF-8")
//    def rfcTitle = issue.projectObject.name + " " + fixVersion
//    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
//    def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()

    //log.warn(cFActualStart)
    //log.warn(Date.parse('yyyy-MM-dd HH:mm:ss', cFActualStart).format("yyyy-MM-dd'T'HH:mm:ssZ"))
    def actStartFormatted = Date.parse('yyyy-MM-dd HH:mm:ss', cFActualStart).format("yyyy-MM-dd'T'HH:mm:ssZ")
    def actEndFormatted = Date.parse('yyyy-MM-dd HH:mm:ss', cFActualEnd).format("yyyy-MM-dd'T'HH:mm:ssZ")

    putChange(actStart: actStartFormatted,
        actEnd: actEndFormatted,
        issues: cfIssues,
        issueDetails: cfIssuesDetails,
        ProgressStatus: deploymentProgress,
        failureReason: cfFailureReason,
        cFRFCNumber: cFRFCNumber, issue)

    postChangeActivity(cFRFCNumber, issue)
}

def putChange(Map m,def issue) {
    def rfcData = [
        Change: [
            ActualImplementationStart : LocalDateTime.now().toString(),
            ActualImplementationEnd : LocalDateTime.now().toString(),
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

    def resp = SMPut("/SM/9/rest/changes/${m.cFRFCNumber}",new Gson().toJson(rfcData))
    return (resp.body?.object?.Change?.header?.ChangeID)
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
//        def activityRFCJson = new JsonBuilder(activityRFC).toString()
//        HttpBuilderUtil.postUtil(restURL, '/SM/9/rest/activitychange', restUser, restPass, activityRFCJson,'JSON')
        def resp = HttpRestUtil.SMPost('/SM/9/rest/activitychange', new Gson().toJson(activityRFC))
    }
}

static def SMPut(String path, String body) {
    String restURL = "http://192.168.47.157:13083"
    String restUser = "Ingjira"
    String restPass = "ELMhpsm@123"
    def authString = HttpRestUtil.getAuthString(restUser,restPass)

    HttpResponse jsonResponse =
        Unirest.post(restURL+path)
            .header('Authorization', "Basic ${authString}")
            .header('Content-Type', ContentType.JSON as String)
            .body(body)
            .asJson()
            .ifSuccess { it.body.object}
            .ifFailure { response ->
                log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                response.getParsingError().ifPresent {
                    log.warn("Parsing Exception: " + it)
                    log.warn("Original body: " + it.getOriginalBody())
                }
            }
    return jsonResponse
}

if(issue.projectObject.key != 'PERP') {
    mainMethod()
}