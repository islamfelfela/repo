package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC


import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.transform.Field
import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor
import com.google.gson.Gson

//@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-23")
@Field  def getInsightObject = CommonUtil.getInsightCField(issue,14805,'Name')

def createChange() {
//    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-23")
    def rfcTitle = issue.key + " : " + "Service-Rollout" + " : " + getInsightObject[0].toString()
    def impactCF = CommonUtil.getCustomFieldValue(issue,10803).toString()
    def changeNumber = postChange(service: "${getInsightObject[0]}",
            issueKey: issue.key,
            assignee: issue.assignee.displayName,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            impact : impactCF.toString(),
            title: rfcTitle, issue)

    if(changeNumber) {
        def files= ComponentAccessor.getAttachmentManager().getAttachments(issue)
        files.each {
            String filePath = getAttachmentFile(issue, it.id.toString()).getPath()
            def attachResp = HttpRestUtil.SMPostAttachment("/SM/9/rest/changes/${changeNumber}/attachments", it.filename, filePath)
        }

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
                    ChangeImpact               : [m.impact],
                    Classification             : "Production",
                    Service                    : affectedCI,
                    "elm.chm.srv.dis.name"     : m.service,
                    "elm.service.environment"  : m.envType,
                    elmchmbusinessunit         : "Technology",
                    elmchmoutage               : "No",
                    elmchmkmtransfer           : "No",
                    elmjiraid                  : m.issueKey,
                    ChangeType                 : "Retire Service",
                    ChangeAuthorization        : "ctype_dpt",
                    "description.structure"    : [
                            Description       : m.issueDesc,
                            BackoutMethod     : m.issueDesc,
                            ImplementationPlan: m.issueDesc
                    ],
                    header                     : [
                            Category    : "Operational Standard",
                            Phase       : "Change Approval",
                            Subcategory : "Service Retirement",
                            InitiatedBy : m.assignee,
                            PlannedStart: m.sysDate,
                            Status      : "initial",
                            Title       : m.title
                    ]
            ]
    ]
    log.warn(new Gson().toJson(rfcData))
    def resp = HttpRestUtil.SMPost('/SM/9/rest/changes/', new Gson().toJson(rfcData))
    log.warn(resp.body?.object)
    return (resp.body?.object?.Change?.header?.ChangeID)
}

def updateIssue(def issue, def changeNumber,def rfcTitle , ApplicationUser executingAdmin){
    def cFRFCNumber = CommonUtil.getCustomFieldObject(11311)
    issue.setCustomFieldValue(cFRFCNumber, changeNumber)
    issue.summary = rfcTitle
    ComponentAccessor.issueManager.updateIssue(executingAdmin, issue, EventDispatchOption.ISSUE_UPDATED, false)
}

File getAttachmentFile(Issue issue, String attachmentId){
    return ComponentAccessor.getComponent(FileSystemAttachmentDirectoryAccessor).getAttachmentDirectory(issue).listFiles().find({
        File it->
            it.getName().equals(attachmentId)
    })
}


createChange()
