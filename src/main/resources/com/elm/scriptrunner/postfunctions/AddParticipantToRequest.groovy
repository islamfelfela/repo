package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

def mainMethod() {

    def issueService = ComponentAccessor.issueService
    def userManager = ComponentAccessor.getUserManager()
    def requestParticipantsField = CommonUtil.getCustomFieldObject(15853) //get Request Participant Object
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters.addCustomFieldValue(requestParticipantsField.id, "halgarni","ahmotaibi", "aabushatat")

    def update = issueService.validateUpdate(Globals.botUser, issue.id, issueInputParameters)
    if (update.isValid()) {
        issueService.update(Globals.botUser, update)
    }
}

mainMethod()