package com.elm.scriptrunner.scriptfields

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.elm.scriptrunner.library.CommonUtil
import groovy.transform.Field
import org.apache.log4j.Logger

@Field NonWorkingDays

//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ELMX-512")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")

def issueDoneDate
def issueCreateDate = issue.created.getTime()

if (!changeItems.isEmpty()) {
    changeItems.each { ChangeItemBean item ->
        if (item.toString == "Done") {
            issueDoneDate = item.created.getTime()
        }
        else if(item.fromString == "Deferred"){
            issueCreateDate = item.created.getTime()
        }
        else {
            issueDoneDate = System.currentTimeMillis()
        }
    }
}
else if (changeItems.isEmpty()) {
    issueDoneDate = System.currentTimeMillis()
}

NonWorkingDays = 0

def rt = (issueDoneDate - issueCreateDate - NonWorkingDays)/1000
log.warn(rt)
if (issue.issueType.name == 'Security Bug') {
    if (issue.priority.name == "High" && rt > 14*86400) {

        return decorateMessage(rt - (14*86400))
    }
    if (issue.priority.name == "Medium" && rt > 30*86400) {

        return decorateMessage(rt - (30*86400))
    }
    if (issue.priority.name == "Low" && rt > 90*86400) {

        return decorateMessage(rt - (90*86400))
    }
    else{
        "<b><font color='green'> Within SLA </b></font>"
    }
}
def static decorateMessage (rtDuration) {
    //"<b><font color='red'>Violated By ${DateUtils.getDurationString((rtDuration) as Long) }</b></font>"
    //"<b><font color='red'>Violated By ${DateUtils.getDurationStringSeconds(rtDuration as Long, Long.MAX_VALUE, Long.MAX_VALUE)}</b></font>"
    "<b><font color='red'>Violated By ${Math.round((rtDuration/86400) as float)}d </b></font>"
}

def countNonWorkingDays(def startcalTime, def endCal) {
    def holidays = ["02/06/2019", "03/06/2019", "04/06/2019", "05/06/2019", "06/06/2019"
                    , "11/08/2019", "12/08/2019", "13/08/2019", "14/08/2019", "15/08/2019", "16/08/2019", "17/08/2019"
                    , "23/09/2019"
    ]

    Calendar startCal = new GregorianCalendar()
    startCal.setTimeInMillis(startcalTime)
    def iHolydayCnt = 0
    while (startCal.getTimeInMillis() < endCal) {
        if (holidays.contains(startCal.getTime().format("dd/MM/YYYY"))) {
            ++iHolydayCnt
        }
        startCal.add(Calendar.DAY_OF_YEAR, 1)
    }
    return iHolydayCnt
}
