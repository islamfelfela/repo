package com.elm.scriptrunner.CdxIntegration.Validators

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.fields.IssueLinksSystemField
import webwork.action.ActionContext
import com.atlassian.jira.issue.IssueFieldConstants

static def validateChangeLinkExist() {
    def request = ActionContext.getRequest()
   // log.warn(request)
    if (!request) {
        def fieldManager = ComponentAccessor.getFieldManager()
        def linksSystemField = fieldManager.getField("issuelinks") as IssueLinksSystemField
        def params = request.getParameterMap()
        def issueLinkingValue = linksSystemField.getRelevantParams(params) as IssueLinksSystemField.IssueLinkingValue
            def storyCount = 0
            if (issueLinkingValue.linkedIssues.size() > 0) {
                //log.warn(issueLinkingValue)
                for (link in issueLinkingValue.linkedIssues) {
                    def object = ComponentAccessor.getIssueManager().getIssueObject(link)
                    if (object.getIssueType().name == 'Change') {
                        storyCount += 1
                    }
                }
                if (storyCount == 0) {
                    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS,
                        "Note: You must link this Problem to a Change.")
                }
            } else {
                throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS,
                    "Note: You must use link type 'related to' relevant Change")
            }
        }
    }


validateChangeLinkExist()
