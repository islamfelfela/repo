package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


/**
 * a post function that updates linked issue custom field
 */

// the name of the field to copy
def  fieldNameToCopy = 16904

// leave blank to copy from the last linked issue (regardless the link type)
final String issueLinkTypeName = "Blocks"

def linkedIssues = ComponentAccessor.issueLinkManager.getOutwardLinks(issue.id)
if (!linkedIssues) {
    log.info "There are no linked issues"
    return
}

if (issueLinkTypeName && !(issueLinkTypeName in linkedIssues*.issueLinkType*.name)) {
    log.info "Could not find any issue, linked with the $issueLinkTypeName issue type"
    return
}

def linkedIssue = issueLinkTypeName ?
        linkedIssues.findAll { it.issueLinkType.name == issueLinkTypeName }.last().destinationObject :
        linkedIssues.last().destinationObject

def CFProjectSizing = CommonUtil.getCustomFieldObject(fieldNameToCopy)
def linkedIssueCustomFieldValue = CommonUtil.getCustomFieldValue(linkedIssue,fieldNameToCopy)
log.warn(linkedIssueCustomFieldValue)

issue.setCustomFieldValue(CFProjectSizing, linkedIssueCustomFieldValue)
ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
