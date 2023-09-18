package com.elm.scriptrunner.CdxIntegration.PostFunctions.Jenkins


import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field

/**
 * a post function that triggers Jenkins Build
 */

@Field String changeStatus = issue.getStatus().name.toString().toLowerCase()
@Field String jenkinBuildURL = CommonUtil.getCustomFieldValue(issue, 13703)
@Field String nextApprovalStageURL
@Field String nextAbortStageURL
@Field def stageId
@Field changeResolution

log.warn("Change ID: ${issue.toString()}")
log.warn("Change Status: ${changeStatus}")
log.warn("Is Change CDX: ${isCdx()}")

if (isCdx()) {
    mainMethod()
}

def mainMethod() {
    getNextPendingAction()
    changeResolution = issue.resolution?.name?.toLowerCase()?.toString()
    switch (changeStatus) {
        case Globals.Status.scheduling: //"scheduling": //Implementation in progress
            if (changeResolution == "fail" || changeResolution == "declined") {
                abortBuild()
            } else if (stageId?.toLowerCase()?.trim() == Globals.JStatus.scheduling.toLowerCase().trim()) {

                approveBuild()
            } else {
                log.warn("Approval stage must be '${Globals.JStatus.scheduling}' | Current stage is:  ${stageId}")
            }
            break
        case "awaiting more information": //Implementation in progress
            abortBuild()
            break
        case Globals.Status.awaitingRelease: //"awaiting release": //HPSM
            if (changeResolution == "fail" || changeResolution == "declined") {
                if (stageId) {
                    abortBuild()
                }
            }
            break
        default:
            log.warn("No mapping status found for execution")
    }
}

def approveBuild() {
    def resumeJenkinBuild
    log.warn("Approval Call: " + nextApprovalStageURL)
    if(nextApprovalStageURL?.toString()!=null) {
        resumeJenkinBuild = HttpRestUtil.JPost(nextApprovalStageURL)
        if (resumeJenkinBuild.status != 200) {
            log.warn("Some error occurred while on approval stage " +
                "Reason: | ${resumeJenkinBuild}")
        }
    }
}

def abortBuild() {
    log.warn("Next Abort Stage URL: ${nextAbortStageURL}")
    if(nextAbortStageURL?.toString()!=null) {
        def resumeJenkinBuild = HttpRestUtil.JPost(nextAbortStageURL)
        log.warn("Build Approval Request Response : ${resumeJenkinBuild.statusText}")
        if (resumeJenkinBuild.status != 200) {
            log.warn("Some error occurred while aborting the build " +
                "Reason: | ${resumeJenkinBuild}")
        }
    }
}

def getNextPendingAction() {
    log.warn("Next Approval URL: ${Constants.JenkinsUrl + jenkinBuildURL}")
    def getNextApprovalStage = HttpRestUtil.JGet(jenkinBuildURL.toString())
    def restResonse = getNextApprovalStage?.status
    def restReader = getNextApprovalStage?.body?.object
    log.warn("NextPending Action Response: ${restResonse}")
    if (restResonse == 200 && restReader) {
        stageId = restReader?.id?.toString()
        nextApprovalStageURL = "${restReader?.redirectApprovalUrl?.toString()}${stageId}/proceedEmpty"
    } else {
        "No jenkins stage found | Reason: ${getNextApprovalStage}"
    }
}

def isCdx() {
    def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
    log.warn("Jenkins Branch Value : ${jenkinBranchValue}")
    log.warn("Is Cdx: ${!(jenkinBranchValue == "null" || jenkinBranchValue == "none")}")
    return !(jenkinBranchValue == "null" || jenkinBranchValue == "none")
}