package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.MutableIssue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

import static calcPriorityValue.*


//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-8")

def calculatedPriorityCF = CommonUtil.getCustomFieldObject(14616)
def calculatedPriorityCfValue = CommonUtil.getCustomFieldValue(issue,14616)

def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def userGroupName = ComponentAccessor.groupManager.getGroupsForUser(currentUser)
    .findAll{it.name.toLowerCase() in[Globals.PPGroups.OPM, Globals.PPGroups.CJ,Globals.PPGroups.BRM] }?.first()?.name?.toLowerCase()
log.warn (userGroupName)

//if (currentUser.username == 'aorwani') {
//    userGroupName = 'customer-journey'
//}

if(calculatedPriorityCfValue >0){
    def addPriorityCfValueToCurrent = getPriorityValue(issue,userGroupName) + calculatedPriorityCfValue as double
    issue.setCustomFieldValue(calculatedPriorityCF, addPriorityCfValueToCurrent)
    log.warn(addPriorityCfValueToCurrent)
}
else {
    issue.setCustomFieldValue(calculatedPriorityCF, getPriorityValue(issue,userGroupName))
    log.warn(getPriorityValue(issue,userGroupName))
}
ComponentAccessor.getIssueManager().updateIssue(Globals.powerUser, issue as MutableIssue, EventDispatchOption.ISSUE_UPDATED, false)
