package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException
import com.atlassian.jira.issue.fields.FixVersionsField
import webwork.action.ActionContext
import com.atlassian.jira.issue.IssueFieldConstants


def mainMethod() {
    def request = ActionContext.getRequest()
    log.warn(request)
    if (!request) {
        def fieldManager = ComponentAccessor.getFieldManager()
        def linksSystemField = fieldManager.getField("fixVersions") as FixVersionsField
        def params = request.getParameterMap()
        def issueLinkingValue = linksSystemField.getRelevantParams(params) as FixVersionsField.
        def customFieldManager = ComponentAccessor.getCustomFieldManager()
        def regressionBug = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12007)).toString()
        if (regressionBug == "No") {
            def storyCount = 0
            if (issueLinkingValue.linkedIssues.size() > 0) {
                log.warn(issueLinkingValue)
                for (link in issueLinkingValue.linkedIssues) {
                    def object = ComponentAccessor.getIssueManager().getIssueObject(link)
                    if (object.getIssueType().name == 'Story') {
                        storyCount += 1
                    }
                }
                if (storyCount == 0) {
                    throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS,
                        "Note: You must link this Bug to a Story.")
                }
            } else {
                //  log.debug(issueLinkingValue.linkedIssues.size())
                throw new InvalidInputException(IssueFieldConstants.ISSUE_LINKS,
                    "Note: You must use link type 'related to' relevant Story")
            }
        }
    }
}

mainMethod()
