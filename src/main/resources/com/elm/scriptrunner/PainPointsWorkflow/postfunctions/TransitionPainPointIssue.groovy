package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field
import java.time.LocalDateTime

CommonUtil.executeScriptWithAdmin('atlassbot')

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("DOJ-28")

//def linkedPainPoints =  ComponentAccessor.getIssueLinkManager().getLinkCollectionOverrideSecurity(issue)?.allIssues?.find {it.issueType.name == 'Pain Points'}

def linkedPainPoints =  ComponentAccessor.issueLinkManager.getInwardLinks(issue.id).find{it.sourceObject.issueType.name == 'Product-PainPoint'}
log.warn(linkedPainPoints.sourceObject.key)
log.warn(linkedPainPoints.destinationObject.key)

if(linkedPainPoints) {
    linkedPainPoints.each {
        def ACTION_ID
        def status = issue.status.name.toLowerCase()
        if (status == 'awaiting business approval') {
            ACTION_ID = 181 //move it to Awaiting Business Approval
            CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.sourceId, ACTION_ID, issue.resolution.id)

        } else if (status == 'awaiting more information') {
            ACTION_ID = 111 //move it to Awaiting more Information
            CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.sourceId, ACTION_ID, issue.resolution.id)

        } else if (status == 'development') {
            ACTION_ID = 71 //move it to In Progress
            CommonUtil.transitionIssue(Globals.botUser, linkedPainPoints.sourceId, ACTION_ID)

        } else if (status == 'testing') {
            ACTION_ID = 101 //move it to Awaiting Release
            CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.sourceId, ACTION_ID, issue.resolution.id)

        } else if (status == 'done') {
            ACTION_ID = 171 //move it to Done
            CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.sourceId, ACTION_ID, issue.resolution.id)

//        issueInputParameters.setResolutionId('11700')
        } else {
            log.warn('No action meet the conditions')
        }

        log.warn(ACTION_ID)

//        CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.destinationId, ACTION_ID, issue.resolution.id)

    }
}
