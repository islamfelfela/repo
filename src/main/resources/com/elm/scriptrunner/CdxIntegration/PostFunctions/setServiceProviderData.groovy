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

@Field  def serviceObject = CommonUtil.getInsightCField(issue,14805,'Name')

def updateIssue(){
    def issueService = ComponentAccessor.issueService
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    log.warn(getServiceProvider(issue))
    issueInputParameters.addCustomFieldValue(16807, getServiceProvider(issue) as String)

    def update = issueService.validateUpdate(Globals.botUser, issue.id, issueInputParameters)
    if (update.isValid()) {
        issueService.update(Globals.botUser, update)
    }

}

updateIssue()

def getServiceProvider(def issue) {

    def serviceProvider = CommonUtil.getInsightObjectByAttributeValue("""objectType = "Service" AND "Name" = "${serviceObject[0]}" """, 1)
    if (serviceProvider) {
        return CommonUtil.getInsightCFValueSpecificAttribute(serviceProvider.id, 'ServiceProvider').first().toString()
    }
    else {
        return 'no service Provider value exist '
    }
}
