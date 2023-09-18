package com.elm.scriptrunner.CdxIntegration.PostFunctions.Incident.Test

import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field
import groovyx.net.http.ContentType
import com.atlassian.jira.component.ComponentAccessor
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import com.google.gson.Gson

def requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()


def mainMethod() {

    def incidentID = createIncident(issue)

}

def createIncident(def issue){
    def lastComment = ComponentAccessor.getCommentManager().getLastComment(issue).body

    def rfcData = [
        Incident: [
            Area                : 'Non-Production Systems', //getCascadeCustomFieldValue().get(CascadingSelectCFType.PARENT_KEY).toString(),
            AssignmentGroup     : 'DCO_INFRA', //"HELPDESK",
            CaseType            : "Technical",
            Category            : "elmincident",
            Service             : 'Non-Production Systems', //getCascadeCustomFieldValue().get(CascadingSelectCFType.PARENT_KEY).toString(),
            Subarea             : getCascadeCustomFieldValue().get(CascadingSelectCFType.PARENT_KEY).toString(), // 'CIT_SAP',
            ProblemType         : getCascadeCustomFieldValue().get(CascadingSelectCFType.CHILD_KEY).toString(), // 'SAP',
            Medium	            : 'Jira',
            TicketOwner         : issue.reporter.displayName,
            Title               : issue.summary+28,
            PriorityCode        : '4', //issue.priority.id,
            Impact              : '3', //issue.priority.id,
            elmjiraid           : issue.key,
            CustomerName        : issue.reporter.displayName,
            Description         : issue.description,
            Status 				: "Information Provided",
            JournalUpdates      : lastComment.toString()
        ]
    ]
    log.warn(new Gson().toJson(rfcData))
    def resp = SMPut('/SM/9/rest/incidentDetail/IM906602',new Gson().toJson(rfcData))
    return  resp

}

static def SMPost(String path, String body) {
    HttpResponse jsonResponse =
        Unirest.post(Constants.smTestUrl+path)
            .header('Authorization', "Basic ${HttpRestUtil.smTestAuthString}")
            .header('Content-Type', ContentType.JSON as String)
            .body(body)
            .asJson()
            .ifFailure { response ->
                log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                response.getParsingError().ifPresent {
                    log.warn("Parsing Exception: " + it)
                    log.warn("Original body: " + it.getOriginalBody())
                }
            }
    return jsonResponse
}

static def SMPut(String path, String body) {
    HttpResponse jsonResponse =
        Unirest.post(Constants.smTestUrl+path)
            .header('Authorization', "Basic ${HttpRestUtil.smTestAuthString}")
            .header('Content-Type', ContentType.JSON as String)
            .body(body)
            .asJson()
            .ifFailure { response ->
                log.warn("Oh No! Status" + ' ' + response.status + ', ' + response.statusText)
                response.getParsingError().ifPresent {
                    log.warn("Parsing Exception: " + it)
                    log.warn("Original body: " + it.getOriginalBody())
                }
            }
    return jsonResponse
}

def getCascadeCustomFieldValue(){
    def serviceCF = CommonUtil.getCustomFieldValue(issue,14619)

    HashMap<String, Option> hashMapEntries = (HashMap<String, Option>) serviceCF
    if (hashMapEntries != null) {
        Option parent = hashMapEntries.get(CascadingSelectCFType.PARENT_KEY)
        Option child = hashMapEntries.get(CascadingSelectCFType.CHILD_KEY)
        log.warn("Cascading values selected: ${parent} - ${child}")
    }

    return hashMapEntries
}

def updateIssue(def issue, def incidentID, ApplicationUser executingAdmin){
    def cFIncidentNo = CommonUtil.getCustomFieldObject(14622)
    issue.setCustomFieldValue(cFIncidentNo, incidentID)
    ComponentAccessor.issueManager.updateIssue(executingAdmin, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

if (requestType == Globals.RequestTypes.enviromentSupport) {
    mainMethod()
}