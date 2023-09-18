package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
import groovy.transform.Field

import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

// @Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-1639")
@Field  def getInsightObject = CommonUtil.getInsightCField(issue,14805,'Name')

def updateChange() {
//    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SR-1639")
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def cFServiceSupportedBy = CommonUtil.getCustomFieldValue(issue,16906).toString()

    def changeNumber = putChange(service: "${getInsightObject[0]}",
            issueKey: issue.key,
            assignee: issue.assignee.displayName,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            title: rfcTitle,
            releaseNote: releaseNoteURL,
            supportedBy : cFServiceSupportedBy,
            cFRFCNumber: cFRFCNumber)

    if(changeNumber) {
        def files= ComponentAccessor.getAttachmentManager().getAttachments(issue)
        files.each {
            String filePath = getAttachmentFile(issue, it.id.toString()).getPath()
            def attachResp = HttpRestUtil.SMPostAttachment("/SM/9/rest/changes/${changeNumber}/attachments", it.filename, filePath)
        }

    }
    postChangeActivity(cFRFCNumber, issue)
}

def putChange(Map m) {
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
            elmjiraid                  : m.issueKey,
            ChangeType                 : "Rollout Service",
            "elm.jira.status"          : "Required Information Provided",
            elmchmkmtransfer           : m.supportedBy,
            ChangeAuthorization        : "ctype_dpt",
            "description.structure"    : [
                    Description       : m.issueDesc,
                    BackoutMethod     : m.issueDesc,
                    ImplementationPlan: m.issueDesc
            ],
            header                     : [
                    Category    : "Operational Standard",
                    Phase       : "Change Approval",
                    Subcategory : "Service Publishing",
                    InitiatedBy : m.assignee,
                    PlannedStart: m.sysDate,
                    Status      : "initial",
                    Title       : m.title
            ]
        ]
    ]

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

updateChange()

File getAttachmentFile(Issue issue, String attachmentId){
    return ComponentAccessor.getComponent(FileSystemAttachmentDirectoryAccessor).getAttachmentDirectory(issue).listFiles().find({
        File it->
            it.getName().equals(attachmentId)
    })
}