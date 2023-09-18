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

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("WAR-590")

def updateChange() {
    CommonUtil.executeScriptWithAdmin()
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()

    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s",""), "UTF-8")
    def rfcTitle = issue.projectObject.name + " " + issue.fixVersions.first().name
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()
    def affectedCI =  'OC_' + issue.projectObject.name
    ApplicationUser assignedDeployerField = (ApplicationUser) CommonUtil.getCustomFieldValue(issue,15400)


    log.warn('assignedDeployerField : '+ assignedDeployerField)

    def changeNumber = postChange(cFRFCNumber: cFRFCNumber,
        service: issue.projectObject.name,
        issueKey        : issue.key,
        Version         : fixVersion,
        reporter        : issue.reporter.displayName,
        issueDesc       : issue.description,
        sysDate         : LocalDateTime.now().toString(),
        title           : rfcTitle,
        releaseNote     : releaseNoteURL, issue,
        assignedDeploy  : assignedDeployerField.displayName,
        affectedCI      :affectedCI)
    if(changeNumber) {
        updateIssue(issue, changeNumber, rfcTitle, releaseNoteURL)
        addReleaseNoteRestriction(issue)
    }
}

def postChange(Map m,def issue) {
    def currentLoggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser().displayName
    def cFSubcategory = CommonUtil.getCustomFieldValue(issue, 12009).toString()
    def elmChmQaRiskIssues = CommonUtil.getCustomFieldValue(issue, 13300).toString()
    def cFQaRiskIssues = elmChmQaRiskIssues == 'Yes' ? CommonUtil.getCustomFieldValue(issue, 12800).toString() : 'NA'
    def elmOutageCF = CommonUtil.getCustomFieldValue(issue, 15304).toString() //Outage Exist

    def elmOutageStartCF = CommonUtil.getCustomFieldValue(issue, 15302).toString() //Outage Start
    def elmOutageEndCF = CommonUtil.getCustomFieldValue(issue, 15303).toString() //Outage End
    def elmImpactMRF = CommonUtil.getCustomFieldValue(issue, 14618).toString() //Delivery Content
    def cFTargetStart = CommonUtil.getCustomFieldValue(issue, 15201).toString()
    def cFTargetEnd = CommonUtil.getCustomFieldValue(issue, 15202).toString()

    def targetStartFormatted = cFTargetStart != 'null' ? Date.parse('yyyy-MM-dd HH:mm:ss', cFTargetStart).format("yyyy-MM-dd'T'HH:mm:ss") : ''
    def targetEndFormatted = cFTargetEnd != 'null' ? Date.parse('yyyy-MM-dd HH:mm:ss', cFTargetEnd).format("yyyy-MM-dd'T'HH:mm:ss") : ''
    def outageStartFormatted = ''
    def outageEndFormatted = ''

    if (elmOutageCF == 'Yes') {
        outageStartFormatted = elmOutageStartCF != 'null' ? Date.parse('yyyy-MM-dd HH:mm:ss', elmOutageStartCF).format("yyyy-MM-dd'T'HH:mm:ss") : ''
        outageEndFormatted = elmOutageEndCF != 'null' ? Date.parse('yyyy-MM-dd HH:mm:ss', elmOutageEndCF).format("yyyy-MM-dd'T'HH:mm:ss") : ''
    }


    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${issue.projectObject.name}" """,1,'ConfigurationItem')
        .first().toString()

    log.warn(affectedCI)

    def rfcData = [
        Change: [
            AcceptanceSucessCriteria   : ["As Per Release Note"],
            ChangeImpact               : ["As Per Release Note"],
            Classification             : 'Production',
            ElmChmQaRiskIssues         : elmChmQaRiskIssues,
            ElmChmQaRiskIssuesDesc     : cFQaRiskIssues,
            Service                    : affectedCI,
            "elm.chm.srv.dis.name"     : m.service,
            "elm.service.environment"  : 'cdx',
            elmchmbusinessunit         : "Technology",
            elmchmoutage               : elmOutageCF,
            "elm.chm.down.start"       : outageStartFormatted,
            "elm.chm.down.end"         : outageEndFormatted,
            "DeploymentGrpNo"            : "1",
            "DeploymentGrp1"           : "SOS_DT",
            "DeploymentGrp1Assignee"   : m.assignedDeploy,
            "AssessmentRisk"           : "No Risk",
            elmchmkmtransfer           : "No",
            elmjiraid                  : m.issueKey,
            "elm.impact.mrf"           : elmImpactMRF,
            "elm.chm.qa.new.release.no": m.Version,
            "description.structure"    : [
                Description       : m.issueDesc,
                BackoutMethod     : "Please visit Release Note Page: " + m.releaseNote,
                ImplementationPlan: "Please visit Release Note Page: " + m.releaseNote
            ],
            header                     : [
                "ChangeCoordinator"     : 'Ahmed Owaid Aldughmani',
                Category                : "Elm Release",
                Phase                   : "Change Implementation",
                Subcategory             : cFSubcategory,
                InitiatedBy             : m.reporter,
                PlannedStart            : targetStartFormatted,
                PlannedEnd              : targetEndFormatted,
                Status                  : "initial",
                Title                   : m.title
            ],
            "middle"                :   [
                assets       :   m.affectedCI
            ]
        ]
    ]


    log.warn(new Gson().toJson(rfcData))
    def resp = HttpRestUtil.SMPut("/SM/9/rest/changes/${m.cFRFCNumber}", new Gson().toJson(rfcData))
    log.warn(resp.body?.object)
    return (resp.body?.object?.Change?.header?.ChangeID)
}

def updateIssue(def issue, def changeNumber,def rfcTitle , def releaseNoteURL) {
    def cFClassificationField = CommonUtil.getCustomFieldObject(12010)
    def cFRFCNumber = CommonUtil.getCustomFieldObject(11311)
    issue.setCustomFieldValue(cFRFCNumber, changeNumber)
    def issueDescWithReleaseNote = issue.description.concat(" \n\r Release Note Page: " + releaseNoteURL)
    issue.setDescription(issueDescWithReleaseNote)
    def availableOptions = ComponentAccessor.optionsManager.getOptions(cFClassificationField.getRelevantConfig(issue))
    def optionToSet = availableOptions.find { it.value == 'Production' }
    cFClassificationField.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(cFClassificationField), optionToSet), new DefaultIssueChangeHolder())
    issue.summary = rfcTitle
    ComponentAccessor.issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

def addReleaseNoteRestriction(def issue){
    ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin()
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

//boolean isCdx() {
//    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
//    log.warn(" Jenkins Branch Value : ${jenkinBranchValue}")
//    log.warn("Is Cdx: ${!(jenkinBranchValue == "" || jenkinBranchValue == "none")}")
//    return !(jenkinBranchValue == "null" || jenkinBranchValue == "none")
//}

if(issue.projectObject.key != 'PERP') {

        updateChange()
}

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