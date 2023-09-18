package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.AfterFailure

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.elm.scriptrunner.library.CommonUtil

//def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("KEFQ-511")

/**
 * a post function that transitions the issue to the next status in the workflow
 */

def issueDetailsCF = CommonUtil.getCustomFieldObject(14603)
def availableOptions = ComponentAccessor.optionsManager.getOptions(issueDetailsCF.getRelevantConfig(issue))
def optionToSet = availableOptions.find { it.value == 'Yes' }

issueDetailsCF.updateValue(null, issue, new ModifiedValue(issue.getCustomFieldValue(issueDetailsCF), optionToSet), new DefaultIssueChangeHolder())