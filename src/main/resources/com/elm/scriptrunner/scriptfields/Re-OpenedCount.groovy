package com.elm.scriptrunner.scriptfields

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import org.apache.log4j.Logger

//Remove the next line for Production
//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("SEEC-636")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def log = Logger.getLogger("com.onresolve.jira.groovy")
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def inTestingStatus = "Testing"
def inDevelopmentStatus = "Development"

def rt = 0

if (changeItems) {
    changeItems.each { ChangeItemBean item ->
        if (item.fromString == inTestingStatus && item.toString == inDevelopmentStatus) {
            rt+= 1
        }
    }
}
else if (!changeItems) {
    return rt
}
