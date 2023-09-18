package com.cloud.ScriptedField

// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import groovy.transform.Field
import org.apache.log4j.Logger
import java.util.concurrent.TimeUnit

//Remove the next line for Production
//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ZAW-143")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def logs = Logger.getLogger("com.onresolve.jira.groovy")
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def inDevelopmentStatus = "Development"
def inTestingStatus = 'Testing'
def inDeploymentStatus = 'Deployment'
def inOpenStatus = "Open"
def rt = []
@Field NonWorkingDays


def changeItems = get('/rest/api/3/issue/{issue.key}/changelog')
    .asObject(List)
    .body as List<Map>

//def createdDateDiff = System.currentTimeMillis() - issue.getCreated().getTime()
//rt << createdDateDiff

if (issue.status.name in ['Awaiting Release' ,'Done']){
    NonWorkingDays = countNonWorkingDays(issue.created.getTime(), changeItems.last().created.getTime())
}else {
    NonWorkingDays = countNonWorkingDays(issue.created.getTime(), System.currentTimeMillis())
}
logs.debug(TimeUnit.MILLISECONDS.toDays(NonWorkingDays))

if (changeItems) {
    def firstFromOpenStatusDate = changeItems.find { ChangeItemBean item ->
        item.fromString = inOpenStatus
    }.created.getTime()
    logs.debug(TimeUnit.MILLISECONDS.toDays(firstFromOpenStatusDate))
    def createdDateDiff = firstFromOpenStatusDate - issue.created.getTime() //- countNonWorkingDays(issue.created.getTime(), firstFromOpenStatusDate)
    rt << createdDateDiff

    if (changeItems.size() >= 1) {
        changeItems.each { ChangeItemBean item ->
            item.fromString

            logs.debug("Date: ${item.created} from :${item.fromString} to : $item.toString")
            long timeDiff = System.currentTimeMillis() - item.created.getTime() //- TimeUnit.DAYS.toMillis(NonWorkingDays)

            if (item.fromString == inDevelopmentStatus) {
                logs.debug(inDevelopmentStatus)
                rt << -timeDiff

            }
            if (item.toString == inDevelopmentStatus) {
                logs.debug(inDevelopmentStatus)
                rt << timeDiff

            }
            if (item.fromString == inDeploymentStatus) {
                logs.debug(inDeploymentStatus)
                rt << -timeDiff

            }
            if (item.toString == inDeploymentStatus) {
                logs.debug(inDeploymentStatus)
                rt << timeDiff

            }
            if (item.fromString == inTestingStatus) {
                logs.debug(inTestingStatus)
                rt << -timeDiff

            }
            if (item.toString == inTestingStatus) {
                logs.debug(inTestingStatus)
                rt << timeDiff

            }
        }
    }
}

else if (!changeItems) {
    def iHolydayCountNoChange = countNonWorkingDays(issue.getCreated().getTime(), System.currentTimeMillis())
    rt << (System.currentTimeMillis() - issue.getCreated().getTime() - iHolydayCountNoChange)
}
logs.debug(NonWorkingDays)

rt << - NonWorkingDays
logs.debug(rt)
def total = rt.sum()
def totalDays = TimeUnit.MILLISECONDS.toDays(total)
logs.debug(totalDays)
switch (issue.priority.name) {
    case { it == "Blocker" && totalDays > 2 }:
        return decorateMessage(totalDays,'icon jqlerror')
    case { it == "High" && totalDays > 10}:
        return decorateMessage(totalDays,'icon jqlerror')
    case { it == "Medium" && totalDays > 22}:
        return decorateMessage(totalDays,'icon jqlerror')
    case { it == "Low" && totalDays > 66}:
        return decorateMessage(totalDays,'icon jqlerror')
    default:
        decorateMessage(totalDays,'icon jqlgood')
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
    <span>${((rtDuration) as float).round(1)}</span>            
    </div>"""
}

def static countNonWorkingDays(def startcalTime, def endCal) {
    def holidays = ["04/06/2019", "05/06/2019", "06/06/2019"
                    , "11/08/2019", "12/08/2019", "13/08/2019", "14/08/2019", "15/08/2019", "16/08/2019", "17/08/2019"
                    , "23/09/2019"
                    , "24/05/2020","25/05/2020","26/05/2020","27/05/2020"
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