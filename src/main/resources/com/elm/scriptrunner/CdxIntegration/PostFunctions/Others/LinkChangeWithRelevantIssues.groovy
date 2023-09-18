package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field
import java.sql.Timestamp
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.IssueLinkTypeManager


/**
 * a post function that links a change issue with relevant issues
 */

def mainMethod() {

    //def jqlQuery = "key = RENC-563"
    def jqlQuery = "type = Change AND Status = Done AND  Resolution = Pass AND 'Build Status' is  EMPTY AND created >= startOfYear() AND issueFunction not in hasLinkType(\"Relates\")"
    def issues =  CommonUtil.findIssues(jqlQuery, Globals.botUser)

    issues.each {sourceIssueObj ->

        def jqlQueryRelatedIssues = "fixVersion = ${sourceIssueObj.fixVersions.first()} AND project  = ${sourceIssueObj.projectObject.key} AND Type != Change"
        def relatedIssues = CommonUtil.findIssues(jqlQueryRelatedIssues, Globals.botUser)


        relatedIssues.each {destinationIssueObj ->
            // the name of the issue link
            final String issueLinkName = "Relates"

            // the sequence of the link
            final Long sequence = 1L

            def loggedInUser = ComponentAccessor.jiraAuthenticationContext.loggedInUser
            def issueLinkTypeManager = ComponentAccessor.getComponent(IssueLinkTypeManager)
            def issueManager = ComponentAccessor.issueManager

            def sourceIssue = issueManager.getIssueByCurrentKey(sourceIssueObj.key)
            def destinationIssue = issueManager.getIssueByCurrentKey(destinationIssueObj.key)
            assert sourceIssue && destinationIssue: "One ore more issues do not exist"

            def availableIssueLinkTypes = issueLinkTypeManager.issueLinkTypes
            def linkType = availableIssueLinkTypes.findByName(issueLinkName)
            assert linkType: "Could not find link type with name $issueLinkName. Available issue link types are ${availableIssueLinkTypes*.name.join(", ")}"

            ComponentAccessor.issueLinkManager.createIssueLink(sourceIssue.id, destinationIssue.id, linkType.id, sequence, loggedInUser)
        }
    }

}

mainMethod()
