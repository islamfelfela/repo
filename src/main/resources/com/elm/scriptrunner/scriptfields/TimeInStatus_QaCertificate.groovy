package com.elm.scriptrunner.scriptfields

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.elm.scriptrunner.library.CommonUtil
import groovy.transform.Field
import java.util.concurrent.TimeUnit

def rt = []

def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def inDevelopmentStatus = "Awaiting QA Certificate"

 //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ISP-6908")

if (changeItems) {
        changeItems.each { ChangeItemBean item ->
            def NonWorkingDays = countNonWorkingDays(item.created.getTime(), System.currentTimeMillis())
            long timeDiff = System.currentTimeMillis() - item.created.getTime() - NonWorkingDays
            if (item.fromString == inDevelopmentStatus) {
                rt << -timeDiff
            }
            if (item.toString == inDevelopmentStatus) {
                rt << timeDiff
            }
        }
}

def total = rt.sum()

if(total > 0) {
    def totalDays = (total / 1000).toLong()
    return totalDays
}



def static countNonWorkingDays(def startcalTime, def endCal) {
    def holidays = ["02/06/2019", "03/06/2019", "04/06/2019", "05/06/2019", "06/06/2019"
                    , "11/08/2019", "12/08/2019", "13/08/2019", "14/08/2019", "15/08/2019", "16/08/2019", "17/08/2019"
                    , "23/09/2019"
                    , "24/05/2020","25/05/2020","26/05/2020","27/05/2020","28/05/2020"
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
    return TimeUnit.DAYS.toMillis(iHolydayCnt)
}