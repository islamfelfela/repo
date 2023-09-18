package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

//def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("KEFQ-511")

def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def assignedDeveloperCF = CommonUtil.getCustomFieldObject(14804)

issue.setCustomFieldValue(assignedDeveloperCF, user)
ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)