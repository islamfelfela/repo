package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.MutableIssue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

def event = event as IssueEvent
log.warn(event.eventTypeId)

def issueObject = event.issue
if (issueObject.issueType.name == 'Change'){
    def fixVersionStartDate = issueObject.fixVersions.first()
    def targetStartDeploymentDate =CommonUtil.getCustomFieldValue(issueObject,15201)
    def targetEndDeploymentDate =CommonUtil.getCustomFieldValue(issueObject,15202)
    def targetStartCF = CommonUtil.getCustomFieldObject(12405)
    def targetEndCF = CommonUtil.getCustomFieldObject(12406)
    if(targetEndDeploymentDate != null && targetStartDeploymentDate !=null) {
        log.warn(issueObject.key + ':' + targetStartDeploymentDate.toString() + ' -- ' + targetEndDeploymentDate.toString())
        issueObject.setCustomFieldValue(targetStartCF, targetStartDeploymentDate)
        issueObject.setCustomFieldValue(targetEndCF, targetEndDeploymentDate)
        ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issueObject as MutableIssue, EventDispatchOption.DO_NOT_DISPATCH, false)
    }
}
