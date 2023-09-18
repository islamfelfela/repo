package com.elm.scriptrunner.CdxIntegration.PostFunctions.Incident

import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType
import com.atlassian.jira.issue.customfields.option.Option
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.atlassian.jira.component.ComponentAccessor
import com.google.gson.Gson



/**
 * a post function that creates an incident in SM
 */

def requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()

def createIncident() {

    def incidentID = sendIncidentDataToSM(issue)
    def aM= ComponentAccessor.getAttachmentManager().getAttachments(issue).get(0).id.toString()

    if (incidentID) {
        String filePath = getAttachmentFile(issue,aM).getPath()

        def attachResp = HttpRestUtil.SMPostAttachment("/SM/9/rest/incidentDetail/${incidentID}/attachments",aM,filePath)

        assert attachResp.status == 200
        updateIssue(issue, incidentID, Globals.powerUser)

    }
}

def sendIncidentDataToSM(def issue){
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
            Description         : issue.description
        ]
    ]
    log.warn(new Gson().toJson(rfcData))
    def resp = HttpRestUtil.SMPost('/SM/9/rest/incidents/',new Gson().toJson(rfcData))
    log.warn(resp.body?.object?.Incident?.IncidentID)
    return  resp.body?.object?.Incident?.IncidentID

}

File getAttachmentFile(Issue issue, String attachmentId){
    return ComponentAccessor.getComponent(FileSystemAttachmentDirectoryAccessor).getAttachmentDirectory(issue).listFiles().find({
        File it->
            it.getName().equals(attachmentId)
    })
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
    createIncident()
}