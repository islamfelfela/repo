package com.elm.scriptrunner.CdxIntegration.PostFunctions.Incident

import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.atlassian.jira.component.ComponentAccessor
import com.google.gson.Gson


/**
 * a post function that updates an incident in SM
 */
def requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()

def updateIncident(def issue){

    def incidentNo = CommonUtil.getCustomFieldValue(issue,14622)
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
            Title               : issue.summary,
            PriorityCode        : '4', //issue.priority.id,
            Impact              : '3', //issue.priority.id,
            elmjiraid           : issue.key,
            CustomerName        : issue.reporter.displayName,
            Description         : issue.description,
            Status 				: "Information Provided",
            JournalUpdates      : getLastCommentByClient(issue)
        ]
    ]
    log.warn(new Gson().toJson(rfcData))
    def resp = HttpRestUtil.SMPut("/SM/9/rest/incidentDetail/${incidentNo}",new Gson().toJson(rfcData))
    return  resp

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

static def updateIssue(def issue, def incidentID, ApplicationUser executingAdmin){
    def cFIncidentStatus = CommonUtil.getCustomFieldObject(14620)
    issue.setCustomFieldValue(cFIncidentStatus, 'Information Provided')
    ComponentAccessor.issueManager.updateIssue(executingAdmin, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

if (requestType == Globals.RequestTypes.enviromentSupport) {
    updateIncident(issue)
}

def getLastCommentByClient(def issue){

    def lastComment = ComponentAccessor.getCommentManager().getLastComment(issue)
    if(lastComment.authorFullName != 'JIRA HPSM Integration'){
        return lastComment.body.toString()
    }
    return 'NA'
}