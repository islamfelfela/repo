package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.issue.ModifiedValue
import org.apache.log4j.Level


def changeHolder = new DefaultIssueChangeHolder()

log.setLevel(Level.DEBUG)

// Get a pointer to the CustomFieldManager
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def sprintValue = ""
def sprintCf = customFieldManager.getCustomFieldObject(customFieldId=10000)

sprintCf.updateValue(null,issue,new ModifiedValue(sprintCf,sprintValue),changeHolder)
