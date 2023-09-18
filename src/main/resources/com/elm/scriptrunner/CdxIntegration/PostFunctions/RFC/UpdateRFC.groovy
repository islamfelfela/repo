package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.google.gson.Gson
import groovy.json.JsonSlurper
import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor


def updateChange() {
    //def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s", ""), "UTF-8")
    def rfcTitle = issue.projectObject.name + " " + fixVersion
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()

    putChange(service: issue.projectObject.name,
            issueKey: issue.key,
            Version: fixVersion,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            title: rfcTitle,
            releaseNote: releaseNoteURL,
            cFRFCNumber: cFRFCNumber, issue)

        postChangeActivity(cFRFCNumber, issue)
        addReleaseNoteRestriction(issue)
}

def putChange(Map m,def issue) {
    def cFClassification = CommonUtil.getCustomFieldValue(issue,12010).toString()
    def cFSubcategory = CommonUtil.getCustomFieldValue(issue,12009).toString()
    def elmImpactMRF = CommonUtil.getCustomFieldValue(issue, 14618).toString() //Delivery Content
    def cFIntegrationAdd = CommonUtil.getCustomFieldValue(issue, 15033).toString() //New Integration added to the service on production?
    def cFFunctionAdd = CommonUtil.getCustomFieldValue(issue, 15011).toString() //New Function/ Component added to the service on production?

    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${issue.projectObject.name}" """,1,'ConfigurationItem')
        .first().toString()

    log.warn(affectedCI)


    def rfcData = [
        Change: [
            AcceptanceSucessCriteria   : ["As Per Release Note"],
            ChangeImpact               : ["As Per Release Note"],
            Classification             : cFClassification,
            Service                    : affectedCI,
            elmchmbusinessunit         : "Technology",
            elmchmoutage               : "No",
            elmchmkmtransfer           : "No",
            elmjiraid                  :  m.issueKey,
            "elm.impact.mrf"           : elmImpactMRF,
            "elm.jira.status"          : "Required Information Provided",
            "elm.chm.qa.new.release.no": m.Version,
            "description.structure"    : [
                Description       : m.issueDesc,
                BackoutMethod     : "Please visit Release Note Page: " + m.releaseNote,
                ImplementationPlan: "Please visit Release Note Page: " + m.releaseNote
            ],
            header                     : [
                Category    : "Elm Release",
                Phase       : "Change Approval",
                Subcategory : cFSubcategory,
                PlannedStart: m.sysDate,
                Status      : "initial",
                Title       : m.title
            ]
        ]
    ]
//    def rfcDataJson = new JsonBuilder(rfcData).toString()
//    def restCall = HttpBuilderUtil.putUtil(restURL,"/SM/9/rest/changes/${m.cFRFCNumber}", restUser,restPass,rfcData,'JSON')
//    log.warn(restCall)
//    return (restCall.reader.Change.header.ChangeID)

    def resp = HttpRestUtil.SMPut("/SM/9/rest/changes/${m.cFRFCNumber}",new Gson().toJson(rfcData))
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
        def resp = HttpRestUtil.SMPost('/SM/9/rest/activitychange', new Gson().toJson(activityRFC))
    }
}

def addReleaseNoteRestriction(def issue){
    ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin()
    def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(),"UTF-8")
    def pKey =  CommonUtil.getProjectKey(issue)
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn(wikiKey)
    def getPageInfo = DoRequestCall.getRestCall('Wiki' , "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")
    def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
    if (pageId) {
        DoRequestCall.putRestCall('', 'Wiki', "rest/experimental/content/${pageId}/restriction/byOperation/update/user?userName=mmojahed")
    }
}

if(issue.projectObject.key != 'PERP') {
    updateChange()
}