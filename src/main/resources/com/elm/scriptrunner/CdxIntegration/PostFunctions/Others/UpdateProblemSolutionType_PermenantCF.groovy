package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

/**
 * a post function that updates the solution type CF to Permanent Solution
 */
def solutionTypeCF = CommonUtil.getCustomFieldObject(13503)
def availableOptions = ComponentAccessor.optionsManager.getOptions(solutionTypeCF.getRelevantConfig(issue))
def optionToSet = availableOptions.find { it.value == 'Permanent Solution' }

solutionTypeCF.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(solutionTypeCF), optionToSet), new DefaultIssueChangeHolder())