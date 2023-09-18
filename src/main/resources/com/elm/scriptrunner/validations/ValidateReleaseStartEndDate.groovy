package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil

def issue = ComponentAccessor.issueManager.getIssueByCurrentKey('')
def cfReleaseDateId = ComponentAccessor.getCustomFieldManager().getCustomFieldObject('releaseDateId')
def cfReleaseDateIdValue = issue.getCustomFieldValue(cfReleaseDateId).toString()

log.warn(cfReleaseDateIdValue)