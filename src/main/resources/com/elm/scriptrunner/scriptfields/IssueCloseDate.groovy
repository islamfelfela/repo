package com.elm.scriptrunner.scriptfields

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager
import org.apache.log4j.Logger
import java.sql.Timestamp

//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("KEFQ-529")
def log = Logger.getLogger("com.onresolve.jira.groovy")
//log.debug(issue.getKey())
ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def closedList = changeHistoryManager.getChangeItemsForField(issue, "status").findAll { it.toString == "Done" }
//log.debug(issue.getStatus().name.toString())
if (closedList.size() >= 1 & issue.getStatus().name.toString() == 'Done') {
    return new Timestamp((closedList.last()?.getCreated()).time)
} else {
    return null
}