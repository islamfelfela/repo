package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil

//def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("KEFQ-511")

def issueService = ComponentAccessor.issueService
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def issueInputParameters = issueService.newIssueInputParameters()
def assigneeToSet = ComponentAccessor.getIssueLinkManager().getLinkCollectionOverrideSecurity(issue)?.allIssues?.getAt(0)?.assignee //Get Assignee Of Issue
log.warn(issue)
if (assigneeToSet != null)
{
    if(issue.assignee==null)
    {
        def validateAssignResult = issueService.validateAssign(user, issue.id, assigneeToSet?.key)
        if (validateAssignResult.isValid()) {
            issueService.assign(user, validateAssignResult)
        }
    }
}