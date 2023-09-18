package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


/**
 * a post function that provides a solution type and updates the resolution value
 */

def solutionTypeCF = CommonUtil.getCustomFieldValue(issue,13503).toString()

if (solutionTypeCF == 'Workaround Solution') {
    issue.setResolutionId('11900')
    ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)

}
else {
log.warn('thanks no need to be updated')
}