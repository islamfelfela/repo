package com.elm.scriptrunner.CdxIntegration.PostFunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")

//CommonUtil.executeScriptWithAdmin('atlassbot')

def updateIssue(){
    def issueService = ComponentAccessor.issueService
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    log.warn(getManager(issue))
    log.warn(getDirector(issue))
    issueInputParameters.addCustomFieldValue(15901, getManager(issue) as String)
    issueInputParameters.addCustomFieldValue(15903, getDirector(issue) as String)

    def update = issueService.validateUpdate(Globals.botUser, issue.id, issueInputParameters)
    if (update.isValid()) {
        issueService.update(Globals.botUser, update)
    }

}

updateIssue()

def getManager(def issue) {
    def mgrUser = CommonUtil.getInsightObjectByAttributeValue("objectType = Employees AND UserName = ${issue.reporter.username}", 1)
    return  CommonUtil.getInsightCFValueSpecificAttribute(mgrUser.id, 'Manager').first().toString()

}

def getDirector(def issue) {
    def dirUser = CommonUtil.getInsightObjectByAttributeValue("objectType = Employees AND UserName = ${issue.reporter.username}", 1)
    return CommonUtil.getInsightCFValueSpecificAttribute(dirUser.id, 'Director').first().toString()

}