package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field
import java.sql.Timestamp


/**
 * a post function that updates the Planned End CF
 */

def mainMethod() {

    //def jqlQuery = "key = TAST-119"
    def jqlQuery = """type = Change AND Status = Done AND created >= startOfYear(-1) AND resolution not in (Declined,Fail)  AND "Build Status" is EMPTY AND category in (PS,TDS,"Product Integration") AND Project not in (RENC,WSL)"""
    def issues =  CommonUtil.findIssues(jqlQuery, Globals.botUser)

    issues.each{
        def cFPlannedStartValue  = CommonUtil.getCustomFieldValue(it, 11100)
        def cFPlannedEndValue  = CommonUtil.getCustomFieldValue(it, 13400)
        def cFTargetDeploymentStart = CommonUtil.getCustomFieldObject(14601)
        def cFTargetDeploymentEnd = CommonUtil.getCustomFieldObject(14602)

        def cFChangeID  = CommonUtil.getCustomFieldValue(it, 11311)

        if (cFChangeID) {
            try {
                def changeRestCall = HttpRestUtil.SMGet('/SM/9/rest/changes/' + cFChangeID)
                log.warn('issueKey: ' + it.key + ', Status: ' + it.status.name + ', ' + changeRestCall.body?.object.Change?.header?.Phase.toString())
                //log.warn(changeRestCall.body?.object.Change)
                def PlannedStart = changeRestCall.body?.object.Change?.ActualImplementationStart?.replace('T', ' ')?.replace('+03:00', '')?.toString()
                def PlannedEnd = changeRestCall.body?.object.Change?.ActualImplementationStart?.replace('T', ' ')?.replace('+03:00', '')?.toString()

                if (PlannedStart && PlannedEnd) {
                    log.warn(PlannedStart)
                    log.warn(PlannedEnd)

//            def targetStartFormatted = Timestamp.valueOf(SMChangeStatus).replace('T',' ').replace('+03:00','')
//            def formattedDate = PlannedEnd.replace('T', ' ').replace('+03:00', '')
//            log.warn(formattedDate)

                    it.setCustomFieldValue(cFTargetDeploymentStart, Timestamp.valueOf(PlannedStart))
                    it.setCustomFieldValue(cFTargetDeploymentEnd, Timestamp.valueOf(PlannedEnd))

                    ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, it, EventDispatchOption.DO_NOT_DISPATCH, false)
                }
            }catch(Exception e ){
                log.warn('most probably implementation not exist')
            }
        }
    }

//    issues.each {
//
//        def jqlQueryRelatedIssues = "fixVersion = ${it.fixVersions.first()}  AND type in (Technical-Task,Story) AND project  = ${it.projectObject.key}"
//        def relatedIssues = CommonUtil.findIssues(jqlQueryRelatedIssues, Globals.botUser)
//
////        def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey()
//        def cFStart = CommonUtil.getCustomFieldValue(it, 14601).toString()
////        def cFTargetDeployment = CommonUtil.getCustomFieldValue(it, 13400).toString()
//        def cFTargetDeploymentValue = CommonUtil.getCustomFieldValue(it, 15202)
//        def cFTargetDeployment = CommonUtil.getCustomFieldObject(15202)
//        //def cFEnd = CommonUtil.getCustomFieldObject(14602)
//        def cFChangeID = CommonUtil.getCustomFieldValue(it, 11311).toString()
//
//        if (cFTargetDeploymentValue) {
//            //def formattedDate = cFPlannedStart.replace('T', ' ').replace('+03:00', '')
//            //log.warn(formattedDate)
//            //def targetStartFormatted = Timestamp.valueOf(formattedDate)
//            for (i in relatedIssues) {
//                log.warn(i.key)
//                i.setCustomFieldValue(cFTargetDeployment, cFTargetDeploymentValue)
//                ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, i, EventDispatchOption.DO_NOT_DISPATCH, false)
//            }
//        } else {
//
//        }
//
////        else {
////            def changeRestCall = HttpRestUtil.SMGet('/SM/9/rest/changes/' + cFChangeID)
////            //log.warn('issueKey: ' + it.key +', Status: ' +  it.status.name + ', ' + changeRestCall.body?.object.Change?.header?.Phase.toString())
////            //log.warn(changeRestCall.body?.object.Change?.header)
////            def SMChangeStatus = changeRestCall.body?.object.Change?.header?.PlannedEnd
////            log.warn(SMChangeStatus)
////            //def targetStartFormatted = Timestamp.valueOf(SMChangeStatus).replace('T',' ').replace('+03:00','')
////            def formattedDate = SMChangeStatus.replace('T', ' ').replace('+03:00', '')
////            log.warn(formattedDate)
////            def targetStartFormatted = Timestamp.valueOf(formattedDate)
////            for (i in relatedIssues) {
////                log.warn(i.key)
////                i.setCustomFieldValue(cFTargetDeployment, targetStartFormatted)
////                ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, i, EventDispatchOption.DO_NOT_DISPATCH, false)
////            }
////        }
//    }

}

mainMethod()
