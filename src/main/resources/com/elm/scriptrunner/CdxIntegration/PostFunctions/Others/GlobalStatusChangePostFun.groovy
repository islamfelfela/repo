package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others


import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


@Field jenkinBuildURL = CommonUtil.getCustomFieldValue(issue, 13703)
@Field String changeStatus = issue.getStatus().name.toString().toLowerCase()

/**
 * a post function that updates status of the change
 */

log.warn("Change ID: ${issue.toString()}")
log.warn("Is Change CDX: ${isCdx()}")
log.warn("Change Status: ${changeStatus.toString()}")

if (isCdx()) {
    switch (changeStatus) {
        case Globals.Status.development:
            //if (stageId.toLowerCase().trim() == implInProgress.toLowerCase().trim()) {
            updateCustomField(Globals.botUser, 13705, "Request sent to Pipeline")
            updateCustomField(Globals.botUser, 13700, "")
            updateCustomField(Globals.botUser, 13703, "")
            break
        case Globals.Status.awaitingQADeploy:
            updateCustomField(Globals.botUser, 13705, "Deploying on QA")
            break

        case Globals.Status.done:
            updateCustomField(Globals.botUser, 13705, "Done")
            break

        default:
            updateCustomField(Globals.botUser, 13705, "Pipeline In Progress")
    }
}

def updateCustomField(def user, long customFieldId, String status) {
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def issueManafer = ComponentAccessor.getIssueManager()
    def cf = customFieldManager.getCustomFieldObject(customFieldId)
    issue.setCustomFieldValue(cf, status)
    issueManafer.updateIssue(user, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
}

def isCdx() {
    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
    log.warn("Jenkins Branch Value : ${jenkinBranchValue}")
    log.warn("Is Cdx: ${!(jenkinBranchValue == "null" || jenkinBranchValue == "none")}")
    return !(jenkinBranchValue == "null" || jenkinBranchValue == "none")
}