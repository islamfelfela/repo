package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

import java.time.LocalDateTime

CommonUtil.executeScriptWithAdmin('atlassbot')
//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-134")

//def linkedPainPoints =  ComponentAccessor.getIssueLinkManager().getLinkCollectionOverrideSecurity(issue)?.allIssues?.find {it.issueType.name == 'Enhancement'}

def linkedPainPoints =  ComponentAccessor.issueLinkManager.getOutwardLinks(issue.id).find{it.sourceObject.issueType.name == 'Product-PainPoint'}
log.warn(linkedPainPoints.sourceObject.key)
log.warn(linkedPainPoints.destinationObject.key)

if(linkedPainPoints) {
    linkedPainPoints.each {
        def ACTION_ID

        if (issue.status.name.toLowerCase() == 'awaiting more information') {
            ACTION_ID = 361 //move it to Done
            log.warn(issue.resolution)
        } else if (issue.status.name.toLowerCase() == 'scheduling') {
            ACTION_ID = 371 //move it to Open
            log.warn(issue.resolution)
        } else if (issue.status.name.toLowerCase() == 'done') {
            ACTION_ID = 401 //move it to Done
            log.warn(issue.resolution)
        } else {

            log.warn(issue.resolution + ' : ' + issue.status.name.toLowerCase())
            log.warn('No action meet the conditions')
            return
        }
        CommonUtil.transitionIssueWithResolution(Globals.botUser, linkedPainPoints.destinationId, ACTION_ID, issue.resolution.id)
    }
}