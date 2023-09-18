package com.elm.scriptrunner.CdxIntegration.Listeners

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionCreateEvent
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil

def issueSummarylist = [
    'Dev Backend Development',
    'QA Backend Development',
    'iOS Publish build for testing',
    'Android Publish build for testing',
    'iOS builds testing',
    'Android builds testing',
    'Change Request creation',
    'QA approval for build/Change',
    'Store Publishing for iOS',
    'Store Publishing for Android'
]

issueSummarylist.each {
    def event = event as VersionCreateEvent
    def changeSummaryTitle = event.version.project.name +'_'+ event.version.name
    IssueService issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters
        .setProjectId(event.version.project.id)
        .setSummary(it == 'Change Request creation'? changeSummaryTitle: it)
        .setIssueTypeId(it == 'Change Request creation'? '11000': '10003') // Technical Task
        .setReporterId('falmasoudi')
        .setFixVersionIds(event.version.id)
    ApplicationUser user = CommonUtil.executeScriptWithAdmin('atlassbot')
    IssueService.CreateValidationResult createValidationResult =
        issueService.validateCreate(user, issueInputParameters)
    if (createValidationResult.isValid()) {
        IssueService.IssueResult createResult = issueService.create(
            user, createValidationResult)
        log.warn(createResult)
        if (!createResult.isValid()) {
            log.warn("Error while creating the issue." + createResult.errorCollection)
        }
    }
    doAfterCreate = {
        def labelManager = ComponentAccessor.getComponent(LabelManager)
        labelManager.addLabel(ComponentAccessor.jiraAuthenticationContext.getLoggedInUser(), issue.getId(), "AutoGenertatedForWarshati", false)
    }

}
