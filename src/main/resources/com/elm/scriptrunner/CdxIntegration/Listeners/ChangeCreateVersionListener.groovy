package com.elm.scriptrunner.CdxIntegration.Listeners

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionCreateEvent
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import org.apache.commons.lang.StringUtils

//@Field ApplicationUser botUser = CommonUtil.executeScriptWithAdmin('bot')

def mainMethod() {
    def event = event as VersionCreateEvent
    String changeSummary = event.version.name.toString()
    long changeVersionId = event.version.id
    long projectId = event.version.project.id
    createChange(projectId, Globals.botUser, changeVersionId, changeSummary)
}


def createChange(long projectId,ApplicationUser botUser,long changeVersionId,String changeSummary) {
    IssueService issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters();
    issueInputParameters
        .setProjectId(projectId)
        .setIssueTypeId("11000")
        .setSummary(changeSummary)
        .setReporterId('falmasoudi')
        .setFixVersionIds(changeVersionId)
    IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(botUser, issueInputParameters)
    if (createValidationResult.isValid()) {
        IssueService.IssueResult createResult = issueService.create(botUser, createValidationResult)
        log.warn("Creation result?: {}"+ createResult.errorCollection.toString())
        if (!createResult.isValid()) {
            log.warn("Something went wrong "+createResult)
        }
    } else {
        String cause = StringUtils.join(createValidationResult.getErrorCollection().getErrorMessages(), "/")
        log.warn("cause :" + cause)
    }
}
mainMethod()