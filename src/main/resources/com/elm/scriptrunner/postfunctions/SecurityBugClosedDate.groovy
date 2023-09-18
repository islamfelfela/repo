package com.elm.scriptrunner.postfunctions
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import org.apache.log4j.Logger
import java.sql.Timestamp
//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("KEFQ-528")
def log = Logger.getLogger("com.onresolve.jira.groovy")
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def issueManafer = ComponentAccessor.getIssueManager()
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def cf = customFieldManager.getCustomFieldObject(13101)
issue.setCustomFieldValue(cf, new Timestamp((new Date()).time))
issueManafer.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)








