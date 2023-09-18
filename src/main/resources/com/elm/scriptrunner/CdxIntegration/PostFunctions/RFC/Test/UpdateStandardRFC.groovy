package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test

import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.google.gson.Gson
import groovy.transform.Field
import groovyx.net.http.ContentType
import kong.unirest.HttpResponse
import kong.unirest.Unirest

import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-23")
@Field  def getInsightObject = CommonUtil.getInsightCField(issue,14805,'Name')

def updateChange() {
//    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def rfcTitle = issue.key + " " + issue.summary
    def releaseNoteURL = CommonUtil.getCustomFieldValue(issue,16401)

    putChange(service: "${getInsightObject[0]}",
            issueKey: issue.key,
            reporter: issue.reporter.displayName,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            title: rfcTitle,
            releaseNote: releaseNoteURL,
            cFRFCNumber: cFRFCNumber)

        postChangeActivity(cFRFCNumber, issue)
}

def putChange(Map m) {
    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${issue.projectObject.name}" """,1,'ConfigurationItem')
        .first().toString()
    log.warn(affectedCI)

    def rfcData = [
            Change: [
                    AcceptanceSucessCriteria   : ["As Per Release Note"],
                    ChangeImpact               : ["As Per Release Note"],
                    Classification             : "Production",
                    Service                    : affectedCI,
                    "elm.chm.srv.dis.name"     : m.service,
                    "elm.service.environment"  : m.envType,
                    elmchmbusinessunit         : "Technology",
                    elmchmoutage               : "No",
                    elmchmkmtransfer           : "No",
                    elmjiraid                  : m.issueKey,
                    ChangeType                 : "Rollout Service",
                    ChangeAuthorization        : "ctype_dpt",
                    "description.structure"    : [
                            Description       : m.releaseNote,
                            BackoutMethod     : "Please visit Release Note Page: " + m.releaseNote,
                            ImplementationPlan: "Please visit Release Note Page: " + m.releaseNote
                    ],
                    header                     : [
                            Category    : "Elm Standard",
                            Phase       : "Change Approval",
                            Subcategory : "Service Rollout",
                            InitiatedBy : m.reporter,
                            PlannedStart: m.sysDate,
                            Status      : "initial",
                            Title       : m.title
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
                type       : "Update from Jira",
                operator   : "elmjirauser",
                description: comment
            ]
        ]
//        def activityRFCJson = new JsonBuilder(activityRFC).toString()
//        HttpBuilderUtil.postUtil(restURL, '/SM/9/rest/activitychange', restUser, restPass, activityRFCJson,'JSON')
//        def resp = SMPost('/SM/9/rest/activitychange', new Gson().toJson(activityRFC))
    }
}

static def SMPut(String path, String body) {
    String restURL = "http://192.168.47.156:13083"
    String restUser = "elmjirauser"
    String restPass = "ELMhpsm@12345"
    def authString = HttpRestUtil.getAuthString(restUser,restPass)

    HttpResponse jsonResponse =
            Unirest.post(restURL+path)
                    .header('Authorization', "Basic ${authString}")
                    .header('Content-Type', ContentType.JSON as String)
                    .body(body)
                    .asJson()
                    .ifSuccess { it.body.object}
//                    .ifFailure { response ->
//                        log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
//                        response.getParsingError().ifPresent {
//                            log.warn("Parsing Exception: " + it)
//                            log.warn("Original body: " + it.getOriginalBody())
//                        }
//                    }
    return jsonResponse
}

updateChange()