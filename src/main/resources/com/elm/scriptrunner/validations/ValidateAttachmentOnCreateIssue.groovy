package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueFieldConstants
import com.atlassian.jira.issue.attachment.TemporaryWebAttachment
import com.atlassian.jira.issue.attachment.TemporaryWebAttachmentManager
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException
import webwork.action.ActionContext


log.warn("VALIDATING 1")
def temporaryAttachmentUtil = ComponentAccessor.getComponent(TemporaryWebAttachmentManager)

def formToken = ActionContext.getRequest()?.getParameter(IssueFieldConstants.FORM_TOKEN)



log.warn("VALIDATING 2 => " + ActionContext.getRequest())

if (formToken) {
    log.warn("VALIDATING 2.1")
    def tempWebAttachments = temporaryAttachmentUtil.getTemporaryWebAttachmentsByFormToken(formToken)
    tempWebAttachments.each { TemporaryWebAttachment it ->
        log.warn("VALIDATING 2.1.1")
        log.warn("Uploaded attachment name: ${it.filename}")
    }

    log.warn("VALIDATING 2.2")

    def attachmentSize = tempWebAttachments.size()
    log.warn("VALIDATING 2.3 => " + tempWebAttachments.size())

    if(attachmentSize >= 1) {
        log.warn("VALIDATING 2.3.1 => SUCCESS")
    } else {
        throw new InvalidInputException(IssueFieldConstants.ATTACHMENT,"SIZE: " + attachmentSize + " - You must add Attachment file!!")
    }
}

log.warn("VALIDATING 3")