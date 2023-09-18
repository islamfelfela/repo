package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test

import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.HttpBuilderUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.google.gson.Gson
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Field
import groovyx.net.http.ContentType

import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor
import org.apache.log4j.Logger

import kong.unirest.HttpResponse
import kong.unirest.Unirest

def mainMethod() {
    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-876")
    ApplicationUser executingAdmin = CommonUtil.executeScriptWithAdmin()
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

    //postChangeActivity(changeNumber: cFRFCNumber, issue)
    //addReleaseNoteRestriction(issue)
}

def putChange(Map m,def issue) {
    def cFClassification = CommonUtil.getCustomFieldValue(issue,12010).toString()
    def cFSubcategory = CommonUtil.getCustomFieldValue(issue,12009).toString()

    def rfcData = [
        Change: [
            AcceptanceSucessCriteria   : ["As Per Release Note"],
            ChangeImpact               : ["As Per Release Note"],
            Classification             : cFClassification,
            Service                    : m.service,
            elmchmbusinessunit         : "Technology",
            elmchmoutage               : "No",
            elmchmkmtransfer           : "No",
            elmjiraid                  :  m.issueKey,
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
    //def rfcDataJson = new JsonBuilder(rfcData).toString()
    //def restCall = HttpBuilderUtil.putUtil(restURL,"/SM/9/rest/changes/${m.cFRFCNumber}", restUser,restPass,rfcData,'JSON')
    def resp = SMPut('/SM/9/rest/changes/'+ m.cFRFCNumber,new Gson().toJson(rfcData))
    return (resp.body)
}

def postChangeActivity(Map m,def issue) {
    def comment = ComponentAccessor.commentManager.getLastComment(issue)?.body
    if (comment) {
        def activityRFC = [
            activitychange: [
                number     : m.changeNumber,
                type       : "Update from Jira",
                operator   : "elmjirauser",
                description: comment
            ]
        ]
        def activityRFCJson = new JsonBuilder(activityRFC).toString()
//        HttpBuilderUtil.postUtil(restURL, '/SM/9/rest/activitychange', restUser, restPass, activityRFCJson,'JSON')
        //HttpRestUtil.doPost(restURL+ '/SM/9/rest/activitychange', activityRFCJson,authString,'JSON')
    }
}

def addReleaseNoteRestriction(def issue){
//    ApplicationUser executingAdmin  = CommonUtil.executeScriptWithAdmin()
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


mainMethod()