package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

//ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")


/**
 * a post function that transitions issues to TESTING status if fixVersion is changed
 */

def changeFixVersion = issue.getFixVersions()
if (!changeFixVersion.isEmpty()) {
    changeFixVersion = changeFixVersion.get(0)
}

def jqlSearch = "project='${issue.getProjectObject().name}' and fixVersion ='${changeFixVersion}'"

def changeIssues = CommonUtil.findIssues(jqlSearch, Globals.botUser)

changeIssues.each { changeIssue ->

    changeIssue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(changeIssue.toString())

    //move stories/bugs to TESTING status if current status is DEPLOYMENT
    if (["story", "bug"].contains(changeIssue.getIssueType().name.toLowerCase().toString())) {
        if (changeIssue.status.name.toString().toLowerCase() == 'deployment') {
            transitionIssues(Globals.botUser, changeIssue, 91)
        } else {
            log.warn("\n-------\nIssueID: ${changeIssue} " +
                    "\nCurent Status: ${changeIssue.status.name.toString()}" +
                    "\nNotes: To transition issue to TESTING status , current status must be DEPLOYMENT\n-------\n")
        }
    }
}

def transitionIssues(botUser, issue, transitionId) {
    def issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.setPriorityId(issue.parentObject.projectId.toString())
    issueInputParameters.setSkipScreenCheck(true)
    def transitionOptions = new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()
    def transitionValidationResult =
            issueService.validateTransition(botUser, issue.id, transitionId, issueInputParameters, transitionOptions)

    if (transitionValidationResult.isValid()) {
        ComponentAccessor.issueManager.updateIssue(botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)
        issueService.transition(botUser, transitionValidationResult).getIssue()
    }
}