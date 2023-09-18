package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.MutableIssue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

import static calcPriorityValue.*


//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-8")


def parentIssueObject = issue.parentObject
def calculatedPriorityCF = CommonUtil.getCustomFieldObject(14616)
def calculatedPriorityCfValue = CommonUtil.getCustomFieldValue(parentIssueObject,14616)

def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()


def addPriorityCfValueToCurrent = getPriorityValue(issue,issue.summary) + calculatedPriorityCfValue as double
parentIssueObject.setCustomFieldValue(calculatedPriorityCF, addPriorityCfValueToCurrent)

if(addPriorityCfValueToCurrent< 49){
    parentIssueObject.setPriorityId('4')
}
else if (addPriorityCfValueToCurrent >= 50 && addPriorityCfValueToCurrent <= 69){
    parentIssueObject.setPriorityId('3')
}else if (addPriorityCfValueToCurrent >= 70 && addPriorityCfValueToCurrent <= 89){
    parentIssueObject.setPriorityId('2')
}else if (addPriorityCfValueToCurrent >= 90){
    parentIssueObject.setPriorityId('10300')
}

log.warn(addPriorityCfValueToCurrent)
ComponentAccessor.getIssueManager().updateIssue(Globals.powerUser, parentIssueObject as MutableIssue, EventDispatchOption.ISSUE_UPDATED, false)
