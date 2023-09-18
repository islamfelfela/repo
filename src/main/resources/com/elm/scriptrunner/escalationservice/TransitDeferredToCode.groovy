package com.elm.scriptrunner.escalationservice

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.history.ChangeItemBean
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field
import org.apache.log4j.Logger


def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItem = changeHistoryManager.getChangeItemsForField(issue, "status").last()
def cwdUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def inStatus = "Deferred"

@Field NonWorkingDays
@Field toDeferred
@Field timeDiff

def ACTION_ID = 131 // Back to Code

if (changeItem.toString == inStatus){
    toDeferred  = changeItem.created.getTime()
    NonWorkingDays = countNonWorkingDays(toDeferred, System.currentTimeMillis())

    timeDiff = (System.currentTimeMillis() - toDeferred - (NonWorkingDays * (60 * 60 * 24 * 1000)))/ (60 * 60 * 24 * 1000)
    log.warn(timeDiff)
}

if (timeDiff > 14){

// this means that all the subtasks have the value 'Yes' checked, therefore return true and make the transition
        def issueService = ComponentAccessor.getIssueService()
        IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
        issueInputParameters.setSkipScreenCheck(true)

        def transitionOptions= new TransitionOptions.Builder()
            .skipConditions()
            .skipPermissions()
            .skipValidators()
            .build()

        def transitionValidationResult =
            issueService.validateTransition(Globals.botUser, issue.id, ACTION_ID, issueInputParameters, transitionOptions)

        if (transitionValidationResult.isValid()) {
            return issueService.transition(Globals.botUser, transitionValidationResult).getIssue()
        }

        log.warn ("Transition of issue ${issue.key} failed. " + transitionValidationResult.errorCollection)
    }

else {
    return "Still not Breached"
}

def static countNonWorkingDays(def startcalTime, def endCal) {
    def holidays = ["02/06/2019", "03/06/2019", "04/06/2019", "05/06/2019", "06/06/2019"
                    , "11/08/2019", "12/08/2019", "13/08/2019", "14/08/2019", "15/08/2019", "16/08/2019", "17/08/2019"
                    , "23/09/2019"
    ]

    Calendar startCal = new GregorianCalendar()
    startCal.setTimeInMillis(startcalTime)
    def iHolydayCnt = 0
    while (startCal.getTimeInMillis() < endCal) {
        if ((startCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
            || (startCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY)
            || holidays.contains(startCal.getTime().format("dd/MM/YYYY"))
        ) {
            ++iHolydayCnt
        }
        startCal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return iHolydayCnt
}
