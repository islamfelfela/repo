package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


/**Update customFieldValue with UpdateIssue**/
def mainMethod() {
    def version = ComponentAccessor.getVersionManager().getVersionsByName('')
    issue.setFixVersions(version)
    //issue.fixVersions.clear()
    ComponentAccessor.issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)
}


mainMethod()