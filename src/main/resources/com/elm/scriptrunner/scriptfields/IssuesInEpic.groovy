package com.elm.scriptrunner.scriptfields


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import groovy.transform.Field
import org.apache.log4j.Logger

//def log = Logger.getLogger("com.onresolve.jira.groovy")
//@Field issue = ComponentAccessor.issueManager.getIssueByCurrentKey("DEVOPS-20")
def sb = new StringBuilder()
IssueLinkTypeManager issueLinkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
IssueLinkManager issueLinkManager = ComponentAccessor.issueLinkManager


def linkType = issueLinkTypeManager.getIssueLinkTypes(false).find { it.name == 'Epic-Story Link' }
def issueLinkList = issueLinkManager.getOutwardLinks(issue.id).findAll { it.getLinkTypeId() == linkType.getId() }
for (issueLink in issueLinkList) {
    def childIssue = issueLink.getDestinationObject()
    sb <<= '<a href=\'https://Jira.elm.sa/browse>'
    sb <<= '\'>'
    sb <<= childIssue.getKey()
    sb <<= '</a> , '
}
return sb
