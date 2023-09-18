package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-30")

CommonUtil.executeScriptWithAdmin('atlassbot')

def linkedPainPoints =  ComponentAccessor.issueLinkManager.getOutwardLinks(issue.id).find{it.sourceObject.issueType.name == 'Product-PainPoint'}

if (!linkedPainPoints) {
    def projectName = CommonUtil.getInsightCField(issue, 14805, 297)
// where service customFieldId = 14805 && serviceName attributeId =297
    def issueLinkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
    log.warn(projectName)
    def projectObject = ComponentAccessor.getProjectManager().getProjectObjByName(projectName)
    final String issueLinkName = "Relates"
    final Long sequence = 1L

    IssueService issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters
        .setProjectId(projectObject.id)
        .setSummary(issue.summary)
        .setDescription(issue.description)
        .setIssueTypeId('11802') // PainPoint Issue Type
        .setReporterId(issue.reporter.username)
        .setPriorityId(issue?.priority?.id)

    IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(Globals.botUser, issueInputParameters)
    assert createValidationResult.valid: createValidationResult.errorCollection

    IssueService.IssueResult createResult = issueService.create(Globals.botUser, createValidationResult)

    if (createResult.valid) {
        def availableIssueLinkTypes = issueLinkTypeManager.issueLinkTypes
        def linkType = availableIssueLinkTypes.findByName(issueLinkName)
        ComponentAccessor.issueLinkManager.createIssueLink(issue.id, createResult.issue.id, linkType.id, sequence, Globals.botUser)


    } else {
        log.warn(createResult.errorCollection)
    }
}