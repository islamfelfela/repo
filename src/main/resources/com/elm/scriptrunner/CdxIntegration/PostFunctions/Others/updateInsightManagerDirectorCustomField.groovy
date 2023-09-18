package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

/**
 * a post function that updates insight manager director custom field
 */

def issueListForEscalation  = CommonUtil.findIssues(""" type = "Security Bug" And status != Done") """, Globals.botUser)

issueListForEscalation.each {issue ->
    if(issue.assignee !=null) {
        updateCustomField(Globals.botUser, 15901, issue)
    }
}

def updateCustomField(def user, long customFieldId,def issue) {
   // def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ZAW-1732")
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def issueManager = ComponentAccessor.getIssueManager()
    def cf = customFieldManager.getCustomFieldObject(customFieldId)

    def cFManagerValue = CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Employees" AND "UserName" = "${issue.assignee.username}" """,1,'Manager')
    ApplicationUser itMgrSourceFieldValue = ComponentAccessor.getUserManager().getUserByName(cFManagerValue.first().toString())
    issue.setCustomFieldValue(cf, itMgrSourceFieldValue)
    issueManager.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}
