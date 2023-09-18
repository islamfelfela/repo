package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.AttachmentManager
import com.atlassian.jira.issue.IssueInputParameters
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-4118")
CommonUtil.executeScriptWithAdmin('atlassbot')

def projectNameList = CommonUtil.getInsightCField(issue,14805,'Key')
def PainPointProjectId  = 16212

log.warn(projectNameList)

if(projectNameList.size()>1) {
    projectNameList.eachWithIndex { it, index ->
    if (index != 0) {
        def insightObj = CommonUtil.getInsightCFieldObject(issue,"key= ${it}",1).first().name
        log.warn(insightObj)
//        if (index == 1) {
            IssueService issueService = ComponentAccessor.getIssueService()
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
            issueInputParameters.setProjectId(PainPointProjectId)
            issueInputParameters.setSummary(issue.summary)
            issueInputParameters.setDescription(issue.description)
            issueInputParameters.setIssueTypeId('11802') // PainPoint Issue Type
            issueInputParameters.setReporterId(issue.reporter.username)
            issueInputParameters.setPriorityId(issue.priority.id)
            issueInputParameters.addCustomFieldValue(14805, insightObj) //"Zawil Demo"
            issueInputParameters.setSkipScreenCheck(true)

            IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(Globals.botUser, issueInputParameters)
            if (createValidationResult.isValid()) {
                IssueService.IssueResult createResult = issueService.create(Globals.botUser, createValidationResult)
                log.warn(createResult.errorCollection)
                if (createResult.isValid()) {
                    AttachmentManager attachmentManager = ComponentAccessor.getAttachmentManager();
                    attachmentManager.copyAttachments(issue, Globals.botUser, createResult.issue.key);

                }
            }
        }

    }

    def insightObj = CommonUtil.getInsightCFieldObject(issue,"key= ${projectNameList.first()}",1)
    def issueManager = ComponentAccessor.getIssueManager()
    def serviceCF = CommonUtil.getCustomFieldObject(14805)
    issue.setCustomFieldValue(serviceCF, insightObj)
    issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}
