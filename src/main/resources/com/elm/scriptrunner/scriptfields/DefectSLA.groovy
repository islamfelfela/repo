package com.elm.scriptrunner.scriptfields

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import java.util.concurrent.TimeUnit

//@Field def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ZAW-863")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def endStatus = 'Done'

def rt = []
def workingMinutes = new WokringMinutesCalculator()

if (changeItems) {
    if (issue.status.name == endStatus){
        rt << workingMinutes.getWorkingMinutes(issue.created,changeItems.last().created)
    }
    else {
        rt << workingMinutes.getWorkingMinutesSince(issue.getCreated())
    }

} else if (!changeItems) {
    rt << workingMinutes.getWorkingMinutesSince(issue.getCreated())
}
def total = rt.sum()
def totalDays = total/60

if (issue.issueType.name=='Problem') {
    /////******************       https://wiki.elm.sa/display/OPS/Problem+Management     *********//////////////////////
    switch (issue.priority.name) {
        case { it == "Blocker" && totalDays > 16}:
            return decorateMessage(totalDays,'icon jqlerror')
        case { it == "High" && totalDays > 58 }:
            return decorateMessage(totalDays,'icon jqlerror')
        case { it == "Medium" && totalDays > 117 }:
            return decorateMessage(totalDays,'icon jqlerror')
        case { it == "Low" && totalDays > 176 }:
            return decorateMessage(totalDays,'icon jqlerror')
        default:
            decorateMessage(totalDays,'icon jqlgood')
    }
}
else {
    switch (issue.priority.name) {
        case { it == "Blocker" && totalDays > 16}:
            return decorateMessage(-totalDays,'icon jqlerror')
        case { it == "High" && totalDays > 40}:
            return decorateMessage(-totalDays,'icon jqlerror')
        case { it == "Medium" && totalDays > 80}:
            return decorateMessage(-totalDays,'icon jqlerror')
        case { it == "Low" && totalDays > 240}:
            return decorateMessage(-totalDays,'icon jqlerror')
        default:
            decorateMessage(totalDays,'icon jqlgood')
    }
}

def static decorateMessage(rtDuration ,status) {
    """<div style="
    -webkit-box-sizing: content-box;
    -moz-box-sizing: content-box;
    box-sizing: 
    content-box;width: 70px;
    padding: 5px;
    overflow: hidden;
    border: 1px dotted;
    -webkit-border-radius: 6px;
    border-radius: 6px; 
    text-align: left;
    -o-text-overflow: ellipsis;
    text-overflow: ellipsis;
    background: white;">            
    <span class="${status}"></span>
    <span>${(rtDuration as float).round(1)}</span>            
    </div>"""
}

def static countNonWorkingDays(def startcalTime, def endCal) {
    def holidays = ["02/06/2019", "03/06/2019", "04/06/2019", "05/06/2019", "06/06/2019"
                    , "11/08/2019", "12/08/2019", "13/08/2019", "14/08/2019", "15/08/2019", "16/08/2019", "17/08/2019"
                    , "23/09/2019"
                    , "24/05/2020","25/05/2020","26/05/2020","27/05/2020","28/05/2020"
                    , "29/07/2020", "30/07/2020","02/08/2020","03/08/2020", "04/08/2020"
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

    iHolydayCnt.each {

    }
    return TimeUnit.DAYS.toMillis(iHolydayCnt)
}