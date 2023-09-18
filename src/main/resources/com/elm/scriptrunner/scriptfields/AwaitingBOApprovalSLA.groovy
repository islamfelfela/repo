
// Required Imports
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.history.ChangeItemBean
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.scriptfields.WokringMinutesCalculator

import java.sql.Timestamp
import java.util.concurrent.TimeUnit

//@Field def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ZAW-863")
def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
def changeItems = changeHistoryManager.getChangeItemsForField(issue, "status")
def InBusinessApproval = "Awaiting Business Approval"


if (issue.status.name == InBusinessApproval) {
    if (changeItems) {
        def toBusinessApproval = changeItems.reverse().find { it.toString == InBusinessApproval }?.created
        log.warn('To : ' + toBusinessApproval)
        if (toBusinessApproval != null) {
            def workingMinutes = new WokringMinutesCalculator().getWorkingMinutesSince(toBusinessApproval)
            log.warn(workingMinutes)
            if (workingMinutes) {
                return ((workingMinutes * 60).toLong())
            } else {
                return 0
            }
        }
    }
}else if(issue.status.name != InBusinessApproval){
    def rt = []
    if (changeItems) {
        changeItems.each { ChangeItemBean item ->
            //def NonWorkingDays = countNonWorkingDays(item.created.getTime(), System.currentTimeMillis())
            long timeDiff =  new WokringMinutesCalculator().getWorkingMinutes(item.created,new Timestamp(System.currentTimeMillis()))
            if (item.fromString == InBusinessApproval) {
                rt << -timeDiff
            }
            if (item.toString == InBusinessApproval) {
                rt << timeDiff
            }
        }
    }
    def total = rt.sum()
    if(total > 0) {
        def totalDays = (total * 60).toLong()
        return totalDays
    }
}