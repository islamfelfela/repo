package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.project.ProjectManager
import com.atlassian.jira.util.SimpleErrorCollection
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CPP-5")

def cFPainPointsApprover = CommonUtil.getCustomFieldObject(14617)
def cFPainPointsApproverValue = CommonUtil.getCustomFieldValue(issue,14617).toString()
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def userGroup = ComponentAccessor.groupManager.getGroupsForUser(currentUser).find{it.name == 'atlassian_admin_users'}.name
//def userGroup = getUserGroup(currentUser)
//ProjectManager projectManager = ComponentAccessor.getComponent(ProjectManager)
//def currentUserRole = projectManager.getProjectRoles(currentUser,issue.projectId,  new SimpleErrorCollection()).toString()

def  cFPainPointsApproverValueList = cFPainPointsApproverValue.split(',').toList()

if(cFPainPointsApproverValue != 'null'){
    log.warn(cFPainPointsApproverValue)
    if (!(userGroup in cFPainPointsApproverValueList)){
        def addUserToPainPointsApprover = cFPainPointsApproverValue.concat(','+ userGroup)
        issue.setCustomFieldValue(cFPainPointsApprover, addUserToPainPointsApprover)
        log.warn(currentUser)
        log.warn([cFPainPointsApproverValue])

    }

}
else {
    issue.setCustomFieldValue(cFPainPointsApprover, userGroup)
    log.warn(userGroup)

}
ComponentAccessor.getIssueManager().updateIssue(Globals.powerUser, issue, EventDispatchOption.ISSUE_UPDATED, false)

