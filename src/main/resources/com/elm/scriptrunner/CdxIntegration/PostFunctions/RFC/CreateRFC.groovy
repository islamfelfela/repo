package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.elm.scriptrunner.scriptfields.WokringMinutesCalculator
import com.google.gson.Gson
import groovy.json.JsonSlurper
import groovy.transform.Field

import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")

/**
 * This function creates Release RFC on SM using Rest API call
 */
def createChange() {
    //CommonUtil.executeScriptWithAdmin()
    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s",""), "UTF-8")
    def rfcTitle = issue.projectObject.name + " " + issue.fixVersions.first().name
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()
    def enviromentType = 'production'
    def changeNumber = postChange(service: issue.projectObject.name,
        issueKey: issue.key,
        Version: fixVersion,
        reporter: issue.reporter.displayName,
        issueDesc: issue.description,
        sysDate: LocalDateTime.now().toString(),
        title: rfcTitle,
        envType:enviromentType,
        releaseNote: releaseNoteURL, issue)
    if(changeNumber) {
        updateIssue(issue, changeNumber, rfcTitle, Globals.powerUser, releaseNoteURL)
        addReleaseNoteRestriction(issue)
    }
}

/**
 * This is the rest API call for SM
 */
def postChange(Map m,def issue) {
    def cFClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()
    def cFSubcategory = CommonUtil.getCustomFieldValue(issue, 12009).toString()
    def elmChmQaRiskIssues = CommonUtil.getCustomFieldValue(issue, 13300).toString()
    def cFQaRiskIssues = elmChmQaRiskIssues == 'Yes' ? CommonUtil.getCustomFieldValue(issue, 12800).toString() : 'NA'
    def elmImpactMRF = CommonUtil.getCustomFieldValue(issue, 14618).toString() //Delivery Content

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
    def resp = HttpRestUtil.SMPost('/SM/9/rest/changes/', new Gson().toJson(rfcData))
    log.warn(resp.body?.object)
    return (resp.body?.object?.Change?.header?.ChangeID)
}

/**
 * After creating RFC , this function update the issue with the returned values
 */
def updateIssue(def issue, def changeNumber,def rfcTitle , ApplicationUser executingAdmin, def releaseNoteURL){
    CommonUtil.executeScriptWithAdmin()
    def cFClassificationField = CommonUtil.getCustomFieldObject(12010)
    def cFRFCNumber = CommonUtil.getCustomFieldObject(11311)
//    def cFBusinessApproval = CommonUtil.getCustomFieldObject(14624)
//    def cFBusinessApprovalValue = issue.getCustomFieldValue(cFBusinessApproval) == null ? 0 : issue.getCustomFieldValue(cFBusinessApproval)
    issue.setCustomFieldValue(cFRFCNumber, changeNumber)
    def issueDescWithReleaseNote = issue.description.concat(" \n\r Release Note Page: "+ releaseNoteURL)
    issue.setDescription(issueDescWithReleaseNote)

    if (isCdx()) {
        def availableOptions = ComponentAccessor.optionsManager.getOptions(cFClassificationField.getRelevantConfig(issue))
        def optionToSet = availableOptions.find { it.value == 'Production' }
        cFClassificationField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(cFClassificationField), optionToSet), new DefaultIssueChangeHolder())
//        cFBusinessApproval.updateValue(null,issue, new ModifiedValue(issue.getCustomFieldValue(cFBusinessApproval) as double,  getTimeInStatus()),new DefaultIssueChangeHolder())
    }
   //issue.summary = rfcTitle
    ComponentAccessor.issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

/**
 * Here we are adding restriction on the related MRF page
 */
def addReleaseNoteRestriction(def issue){
    CommonUtil.executeScriptWithAdmin()
    def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(),"UTF-8")
    def pKey =  CommonUtil.getProjectKey(issue)
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn(wikiKey)
    log.warn(wikiKey)
    def getPageInfo =DoRequestCall.getRestCall(Globals.wiki , "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")
    log.warn(getPageInfo)
    def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
    if (pageId) {
        DoRequestCall.putRestCall('', Globals.wiki, "rest/experimental/content/${pageId}/restriction/byOperation/update/user?userName=mmojahed")
    }
}


boolean isCdx() {
    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
    log.warn(" Jenkins Branch Value : ${jenkinBranchValue}")
    log.warn("Is Cdx: ${!(jenkinBranchValue == "" || jenkinBranchValue == "none")}")
    return !(jenkinBranchValue == "null" || jenkinBranchValue == "none")
}

if(issue.projectObject.key != 'PERP') {
    def customFieldRFC = CommonUtil.getCustomFieldValue(issue, 11311)
    if (customFieldRFC) {
        new UpdateRFC().updateChange()
    } else {
        createChange()
    }
}

/**
 * get the time in between status Awaiting Business approval
 */
double getTimeInStatus() {
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
