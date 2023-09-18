package com.elm.scriptrunner.scriptfields

import com.atlassian.core.util.DateUtils

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.Field
import java.util.concurrent.TimeUnit


//@Field def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ZAW-1498")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def endStatus = 'Done'

def rt = []
def workingMinutes = new WokringMinutesCalculator()

if (changeItems) {

    rt << workingMinutes.getWorkingMinutes(issue.created, changeItems.last().created)

}
else if (!changeItems) {
    rt << workingMinutes.getWorkingMinutesSince(issue.getCreated())
}

def total = rt.sum()
if(total > 0) {
    def totalDays = (total * 60).toLong()
    return totalDays
}
