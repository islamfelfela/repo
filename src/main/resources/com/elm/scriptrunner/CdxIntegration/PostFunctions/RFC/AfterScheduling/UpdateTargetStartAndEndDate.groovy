package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.AfterScheduling


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

import java.sql.Timestamp

//@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SEN-2634")


/** Get Advanced Road Map Target Start & End Date Object**/
def targetStartCF = CommonUtil.getCustomFieldObject(12405)
def targetEndCF = CommonUtil.getCustomFieldObject(12406)

def issueList = CommonUtil.findIssues('type = Change AND created >= startOfMonth(-6) AND status in ("Awaiting Release", Done) AND resolution in (Pass,Fail) AND "Jenkins Branch" is EMPTY',Globals.botUser)

issueList.each {

    if (it.key == 'ZAW-1608') {
/**** get Planned Start & End Date For Non CDx projects ******/
        def plannedStartCF = CommonUtil.getCustomFieldValue(it, 11100)?.toString()
        def plannedEndCF = CommonUtil.getCustomFieldValue(it, 13400)?.toString()
        if (plannedStartCF != null) {
            def targetStartFormatted = Timestamp.valueOf(plannedStartCF)
            def targetEndFormatted = Timestamp.valueOf(plannedEndCF)
            log.warn(targetStartFormatted)
            log.warn(targetEndFormatted)

            it.setCustomFieldValue(targetStartCF, targetStartFormatted)
            it.setCustomFieldValue(targetEndCF, targetEndFormatted)

        }

        ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, it, EventDispatchOption.DO_NOT_DISPATCH, false)
    }
}


///**** get Planned Start & End Date For Non CDx projects ******/
//def plannedStartCF = CommonUtil.getCustomFieldValue(issue,11100)?.toString()
//def plannedEndCF = CommonUtil.getCustomFieldValue(issue,13400)?.toString()

///**** get Target Deployment Start & End Date For CDx projects ******/
//def targetDeployStartCF = CommonUtil.getCustomFieldValue(issue,15201)?.toString()
//def targetDeployEndCF = CommonUtil.getCustomFieldValue(issue,15202)?.toString()
//
//


//if (plannedStartCF != null) {
//    def targetStartFormatted = Timestamp.valueOf(plannedStartCF)
//    def targetEndFormatted = Timestamp.valueOf(plannedEndCF)
//}
//
//if (plannedStartCF != null) {
//    def targetStartFormatted = Timestamp.valueOf(plannedStartCF)
//    def targetEndFormatted = Timestamp.valueOf(plannedEndCF)
//}

//log.warn(targetStartFormatted)
//log.warn(targetEndFormatted)
//
//issue.setCustomFieldValue(targetStartCF, targetStartFormatted)
//issue.setCustomFieldValue(targetEndCF, targetEndFormatted)
//
//
//ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)

