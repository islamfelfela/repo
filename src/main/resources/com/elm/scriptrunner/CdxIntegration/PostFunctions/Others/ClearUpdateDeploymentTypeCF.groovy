package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


/**
 * a post function that updates the Deployment Type CF to None if Jenkins Branch CF is not None
 */


def jenkinsBranchCF = CommonUtil.getCustomFieldValue(issue,13709)

if (jenkinsBranchCF != 'None' ) {
    def deploymentTypeCF = CommonUtil.getCustomFieldObject(15301)
    issue.setCustomFieldValue(deploymentTypeCF, '')
    ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}
