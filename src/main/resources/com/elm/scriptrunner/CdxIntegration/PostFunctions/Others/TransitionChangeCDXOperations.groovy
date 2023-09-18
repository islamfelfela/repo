package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


/**
 * a post function that transitions the issue to CDx Operations if the issue is CDx
 */
if (isCDx()) {
    transitionIssues(Globals.botUser, 491)
}

def transitionIssues(botUser, transitionId) {
    def issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.setSkipScreenCheck(true)
    def transitionOptions = new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()
    def transitionValidationResult =
            issueService.validateTransition(botUser, issue.id, transitionId, issueInputParameters, transitionOptions)

    if (transitionValidationResult.isValid()) {
        ComponentAccessor.issueManager.updateIssue(botUser, issue as MutableIssue, EventDispatchOption.ISSUE_UPDATED, false)
        def issueResult = issueService.transition(botUser, transitionValidationResult)
        if (!issueResult.isValid()) {

            log.warn("Failed to transition task ${issue.toString()}, errors: ${issueResult.errorCollection}")
        }
    } else {
        log.warn("Error occurred while doing the auto transition" + transitionValidationResult.errorCollection)
    }
}

boolean isCDx() {
    ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")
    def changeProject = CommonUtil.getProjectObjByIssue(issue).key.toString()
    def JJPKeyName = CommonUtil.getCustomFieldObject(13608).name.toString()
    def jenkinBranchName = CommonUtil.getCustomFieldObject(13603).name.toString()
    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString()
    def jql = "'${JJPKeyName}' ~  '${changeProject}' and '${jenkinBranchName}'~'${jenkinBranchValue}'"
    List mappingResults = CommonUtil.findIssues(jql, botUser) as List
    logs("\nChange#: ${issue.toString()}\n" +
            "JQL Query: ${jql}\n" +
            "Project:${changeProject}\n" +
            "Service: ${jenkinBranchValue}\n" +
            "Mapping Results: ${mappingResults}")
    return !mappingResults.isEmpty()
}
