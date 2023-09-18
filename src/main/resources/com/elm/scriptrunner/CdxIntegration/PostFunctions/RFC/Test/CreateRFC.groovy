package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test


import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.DoRequestCall
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

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ZAW-1463")

def createChange() {
//    ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin('bot')
    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s",""), "UTF-8")
    def rfcTitle = issue.projectObject.name + " " + issue.fixVersions.first().name
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()
    def cFActualStart = CommonUtil.getCustomFieldValue(issue, 1110).toString()
    def enviromentType =  'cdx'
    def changeNumber = postChange(service: 'Zawil',
        issueKey: issue.key,
        Version: fixVersion,
        reporter: issue.reporter.displayName,
        issueDesc: issue.description,
        sysDate: LocalDateTime.now().toString(),
        title: rfcTitle,
        enviromentType:enviromentType,
        releaseNote: releaseNoteURL, issue)

    if(changeNumber) {
        //updateIssue(issue, changeNumber, rfcTitle, Globals.botUser)
        //addReleaseNoteRestriction(issue)
    }
}

def postChange(Map m,def issue) {
    def cFClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()
    def cFSubcategory = CommonUtil.getCustomFieldValue(issue, 12009).toString()
    def elmChmQaRiskIssues = CommonUtil.getCustomFieldValue(issue, 13300).toString()
    def cFQaRiskIssues = elmChmQaRiskIssues == 'Yes' ? CommonUtil.getCustomFieldValue(issue, 12800).toString() : 'NA'
    def elmImpactMRF = CommonUtil.getCustomFieldValue(issue, 14618).toString() //Delivery Content
//    def cFTargetStart = CommonUtil.getCustomFieldValue(issue, 15201).toString()
//    def cFTargetEnd = CommonUtil.getCustomFieldValue(issue, 15202).toString()
//    def targetStartFormatted = Date.parse('yyyy-MM-dd HH:mm:ss', cFTargetStart).format("yyyy-MM-dd'T'HH:mm:ss")
//    def targetEndFormatted = Date.parse('yyyy-MM-dd HH:mm:ss', cFTargetEnd).format("yyyy-MM-dd'T'HH:mm:ss")

    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${issue.projectObject.name}" """,1,'ConfigurationItem')
            .first().toString()

    log.warn(affectedCI)




    def rfcData = [
        Change: [
            AcceptanceSucessCriteria   : ["As Per Release Note"],
            ChangeImpact               : ["As Per Release Note"],
            Classification             : cFClassification,
            ElmChmQaRiskIssues         : elmChmQaRiskIssues,
            ElmChmQaRiskIssuesDesc     : cFQaRiskIssues,
            Service                    : affectedCI,
            "elm.chm.srv.dis.name"     : m.service,
            "elm.service.environment"  : m.envType,
            elmchmbusinessunit         : "Technology",
            elmchmoutage               : "No",
            elmchmkmtransfer           : "No",
            elmjiraid                  : m.issueKey,
            "elm.impact.mrf"           : elmImpactMRF,
            "elm.chm.qa.new.release.no": m.Version,
            "elm.jira.sla.hours"       : getTimeInStatus(),
            "description.structure"    : [
                Description       : m.issueDesc,
                BackoutMethod     : "Please visit Release Note Page: " + m.releaseNote,
                ImplementationPlan: "Please visit Release Note Page: " + m.releaseNote
            ],
            header                     : [
                Category    : "Elm Release",
                Phase       : "Change Approval",
                Subcategory : cFSubcategory,
                InitiatedBy : m.reporter,
                PlannedStart: m.sysDate,
                Status      : "initial",
                Title       : m.title
            ]
        ]
    ]
    def resp = SMPost('/SM/9/rest/changes/', new Gson().toJson(rfcData))
    log.warn(resp.body?.object)
    return (resp.body?.object?.Change?.header?.ChangeID)
}

def updateIssue(def issue, def changeNumber,def rfcTitle , ApplicationUser executingAdmin){
    def cFClassificationField = CommonUtil.getCustomFieldObject(12010)
    def cFRFCNumber = CommonUtil.getCustomFieldObject(11311)
    def cFBusinessApproval = CommonUtil.getCustomFieldObject(14624)
    issue.setCustomFieldValue(cFRFCNumber, changeNumber)
    if (isCdx()) {
        def availableOptions = ComponentAccessor.optionsManager.getOptions(cFClassificationField.getRelevantConfig(issue))
        def optionToSet = availableOptions.find { it.value == 'Production' }
        cFClassificationField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(cFClassificationField), optionToSet), new DefaultIssueChangeHolder())
        cFBusinessApproval.updateValue(null,issue, getTimeInStatus() as ModifiedValue,new DefaultIssueChangeHolder())
    }
    issue.summary = rfcTitle
    ComponentAccessor.issueManager.updateIssue(executingAdmin, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def addReleaseNoteRestriction(def issue){
    //ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin()
    def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(),"UTF-8")
    def pKey =  CommonUtil.getProjectKey(issue)
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.debug(wikiKey)
    def getPageInfo =DoRequestCall.getRestCall('Wiki' , "rest/api/content/search?cql=space=${wikiKey}%20and%20title='${originalPageTitle}'")
    def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
    if (pageId) {
        DoRequestCall.putRestCall('', 'Wiki', "rest/experimental/content/${pageId}/restriction/byOperation/update/user?userName=mmojahed")
    }
}

boolean isCdx() {
    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
    log.warn(" Jenkins Branch Value : ${jenkinBranchValue}")
    log.warn("Is Cdx: ${!(jenkinBranchValue == "" || jenkinBranchValue == "none")}")
    return !(jenkinBranchValue == "null" || jenkinBranchValue == "none")
}

static def SMPost(String path, String body) {
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
            .ifFailure { response ->
                log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.body.object.Messages)
                response.getParsingError().ifPresent {
                    log.warn("Parsing Exception: " + it)
                    log.warn("Original body: " + it.getOriginalBody())
                }
            }
    return jsonResponse
}

static def SMGet(String path) {
    String restURL = "http://192.168.47.157:13083"
    String restUser = "Ingjira"
    String restPass = "ELMhpsm@123"
    def authString = HttpRestUtil.getAuthString(restUser,restPass)

    def jsonResponse =
        Unirest.get(Constants.smUrl+path)
            .header('Authorization', "Basic ${authString}")
            .header('Content-Type', ContentType.JSON as String)
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
        createChange()
}

def getTimeInStatus() {
    def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
    def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
    def InBusinessApproval = "Awaiting Business Approval"
    def rt = []

    if (changeItems) {
        def fromBusinessApproval = changeItems.reverse().find { it.fromString == InBusinessApproval }?.created
        def toBusinessApproval = changeItems.reverse().find { it.toString == InBusinessApproval }?.created
        log.warn('To : ' + toBusinessApproval)
        log.warn('From : ' + fromBusinessApproval)
        if (fromBusinessApproval != null && toBusinessApproval != null) {
            def workingMinutes = new WokringMinutesCalculator().getWorkingMinutes(toBusinessApproval, fromBusinessApproval)
            log.warn(workingMinutes)
            if (workingMinutes) {
                return (workingMinutes / 60).round(2)
            } else {
                return 0.1
            }

        }
        else {
            return  0.1
        }
    }
}