package com.elm.scriptrunner.scriptfields

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.link.IssueLinkManager
import com.atlassian.jira.issue.link.IssueLinkTypeManager
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field
import org.apache.log4j.Logger

def sb = new StringBuilder()

//@Field issue = ComponentAccessor.issueManager.getIssueByCurrentKey("DEVOPS-20")
//def  jqlQuery =  "fixVersion = ${issue.fixVersions.first()} AND Type = Change"
def  jqlQuery =  "type = Change AND created >= startOfYear(-1) AND Classification = Production "
def AllReleaseIssues  = CommonUtil.findIssues(jqlQuery, Globals.botUser)

final String issueLinkName = "Relates"
final Long sequence = 1L


if (AllReleaseIssues) {
    for (i in AllReleaseIssues) {
        def cfClassification = CommonUtil.getCustomFieldValue(i, 12010).toString()
        if (cfClassification == 'Production' && i.projectObject.name == i.projectObject.name) {
            //def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
            def jqlQueryRelatedIssues = "fixVersion = ${i.fixVersions.first()} AND project  = ${i.projectObject.key} AND type != Change"
            def relatedIssues =  CommonUtil.findIssues(jqlQueryRelatedIssues, Globals.botUser)

            relatedIssues.each {
                def issueLinkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
                def issueManager = ComponentAccessor.issueManager

                def sourceIssue = issueManager.getIssueByCurrentKey(it.key)
                def destinationIssue = issueManager.getIssueByCurrentKey(i.key)
                assert sourceIssue && destinationIssue: "One ore more issues do not exist"

                def availableIssueLinkTypes = issueLinkTypeManager.issueLinkTypes
                def linkType = availableIssueLinkTypes.findByName(issueLinkName)
                assert linkType: "Could not find link type with name $issueLinkName. Available issue link types are ${availableIssueLinkTypes*.name.join(", ")}"

                ComponentAccessor.issueLinkManager.createIssueLink(sourceIssue.id, destinationIssue.id, linkType.id, sequence, Globals.botUser)
            }
        }
    }
}
