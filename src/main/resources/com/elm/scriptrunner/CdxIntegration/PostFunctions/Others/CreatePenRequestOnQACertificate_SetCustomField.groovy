package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field
import com.atlassian.jira.event.type.EventDispatchOption

//@Field ApplicationUser executingAdmin = CommonUtil.executeScriptWithAdmin("bot")

/**
 * a post function that creates a pen request on QA Certificate
 */

def cFExternalPen =  CommonUtil.getCustomFieldObject(11600)
def cFvalue = ComponentAccessor.optionsManager.getOptions(cFExternalPen.getRelevantConfig(issue)).find{it.value =='No'}

if (issue.creator.username == "bot") {
    def userManager = ComponentAccessor.getUserManager()
    def issueManager = ComponentAccessor.getIssueManager()
    def issueService = ComponentAccessor.issueService
    issue.setCustomFieldValue(cFExternalPen,cFvalue)
    issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)

}