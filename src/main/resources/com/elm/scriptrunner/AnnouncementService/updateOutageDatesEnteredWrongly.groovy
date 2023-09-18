package com.elm.scriptrunner.AnnouncementService


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

/**
 * this script Testing purpose for the function of updating dates entry sent from SM to be matched with JIRA format
 */

def issueList = CommonUtil.findIssues("OutageEnd1 is not EMPTY", Globals.botUser)

issueList.each {issueObject ->

    def issueService = ComponentAccessor.issueService
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()

    def cFOutageDateList = [15915, 15917, 15918, 15919, 15920, 15921, 15922, 15923]

    cFOutageDateList.each {
        def cFValue = CommonUtil.getCustomFieldValue(issueObject, it)?.toString()
        if (cFValue?.contains('1970-01-01')) {
            issueInputParameters.addCustomFieldValue(it, null)
        } else {
            log.warn('Value is ok')
        }
    }

    def updateValidationResult = issueService.validateUpdate(Globals.botUser, issue.id, issueInputParameters)
    assert updateValidationResult.valid: updateValidationResult.errorCollection

    def issueUpdateResult = issueService.update(Globals.botUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
    assert issueUpdateResult.valid: issueUpdateResult.errorCollection

}