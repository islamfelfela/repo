package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.MutableIssue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("DOJ-28")
CommonUtil.executeScriptWithAdmin('atlassbot')

def plannedStartDateCFValue = CommonUtil.getCustomFieldValue(issue,15000) // Plan-Start-Date
def plannedEndDateCFValue = CommonUtil.getCustomFieldValue(issue,15001) // Plan-End-Date

def plannedStartDateCF = CommonUtil.getCustomFieldObject(15000)
def plannedEndDateCF = CommonUtil.getCustomFieldObject(15001)

//def linkedPainPoints =  ComponentAccessor.getIssueLinkManager()
//    .getLinkCollectionOverrideSecurity(issue)?.allIssues?.find{it.issueType.name == 'Pain Points'}

def linkedPainPoints =  ComponentAccessor.issueLinkManager.getInwardLinks(issue.id).find{it.sourceObject.issueType.name == 'Pain Point'}

linkedPainPoints.setCustomFieldValue(plannedStartDateCF, plannedStartDateCFValue)
linkedPainPoints.setCustomFieldValue(plannedEndDateCF, plannedEndDateCFValue)
ComponentAccessor.getIssueManager().updateIssue(Globals.powerUser, linkedPainPoints as MutableIssue, EventDispatchOption.ISSUE_UPDATED, false)
