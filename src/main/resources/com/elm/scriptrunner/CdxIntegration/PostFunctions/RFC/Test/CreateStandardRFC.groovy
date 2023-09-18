package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test


import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.elm.scriptrunner.scriptfields.WokringMinutesCalculator
import groovy.json.JsonSlurper
import groovy.transform.Field
import groovyx.net.http.ContentType
import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import com.google.gson.Gson

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-23")
@Field  def getInsightObject = CommonUtil.getInsightCField(issue,14805,'Name')

def createChange() {
//    ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin('bot')
//    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s",""), "UTF-8")
    def rfcTitle = issue.projectObject.name + " " + issue.key
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def releaseNoteURL = 'Testing Standard RFC' //"https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()
    def cFActualStart = CommonUtil.getCustomFieldValue(issue, 1110).toString()

    def changeNumber = postChange(service: "${getInsightObject[0]}",
            issueKey: issue.key,
            reporter: issue.reporter.displayName,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            title: rfcTitle, issue)

    if(changeNumber) {
        updateIssue(issue, changeNumber, rfcTitle, Globals.botUser)
    }
}

def postChange(Map m,def issue) {
    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${getInsightObject[0]}" """,1,'ConfigurationItem')
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
                            Description       : "Testing",//m.issueDesc,
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
    log.warn(new Gson().toJson(rfcData))
    def resp = SMPost('/SM/9/rest/changes/', new Gson().toJson(rfcData))
    log.warn(resp.body?.object)
    return (resp.body?.object?.Change?.header?.ChangeID)
}

def updateIssue(def issue, def changeNumber,def rfcTitle , ApplicationUser executingAdmin){
    def cFClassificationField = CommonUtil.getCustomFieldObject(12010)
    def cFRFCNumber = CommonUtil.getCustomFieldObject(11311)
    def cFBusinessApproval = CommonUtil.getCustomFieldObject(14624)
    issue.setCustomFieldValue(cFRFCNumber, changeNumber)
    issue.summary = rfcTitle
    ComponentAccessor.issueManager.updateIssue(executingAdmin, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

static def SMPost(String path, String body) {
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
//                    .ifFailure { response ->
//                        log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.body.object.Messages)
//                        response.getParsingError().ifPresent {
//                            log.warn("Parsing Exception: " + it)
//                            log.warn("Original body: " + it.getOriginalBody())
//                        }
//                    }
    return jsonResponse
}

static def SMGet(String path) {
    String restURL = "http://192.168.47.156:13083"
    String restUser = "elmjirauser"
    String restPass = "ELMhpsm@12345"
    def authString = HttpRestUtil.getAuthString(restUser,restPass)

    def jsonResponse =
            Unirest.get(Constants.smUrl+path)
                    .header('Authorization', "Basic ${authString}")
                    .header('Content-Type', ContentType.JSON as String)
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

createChange()
