package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import java.sql.Timestamp


//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("KEFQ-528")
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def issueManafer = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def cf = customFieldManager.getCustomFieldObject(13101)
def dt = new Timestamp((new Date()).time)
issue.setCustomFieldValue(cf, dt.clearTime())
issueManafer.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
