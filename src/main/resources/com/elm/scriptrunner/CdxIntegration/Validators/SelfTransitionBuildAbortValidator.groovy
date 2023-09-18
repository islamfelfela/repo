package com.elm.scriptrunner.CdxIntegration.Validators

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.opensymphony.workflow.InvalidInputException
import groovy.transform.Field
import com.elm.scriptrunner.library.HttpRestUtil


@Field String jenkinBuildURL = CommonUtil.getCustomFieldValue(issue, 13703)
@Field String changeStatus = issue.getStatus().name.toString().toLowerCase()
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
    CommonUtil.executeScriptWithAdmin('atlassbot')
    changeResolution = issue.resolution?.name?.toLowerCase()?.toString()
    getNextPendingAaction()
    log.warn("Change Resolution: ${changeResolution}")
    log.warn("stage id: " + stageId)
    switch (changeStatus) {
        case Globals.Status.awaitingQACertify: //"awaiting qa certificate": //Reject QA Release
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.deployOnQA.toLowerCase().trim() ||
                stageId?.toLowerCase()?.trim() == Globals.JStatus.certifyRelease.toLowerCase().trim()) {
                abortBuild()
            }
            else {
                throw new InvalidInputException("Pipeline stage must be '${Globals.JStatus.deployOnQA}' or '${Globals.JStatus.certifyRelease}'| Current stage is:  ${stageId}")
            }
            break

        default:
            throw new InvalidInputException("You are trying to execute the status other than the allowed one, please contact techsupport@elm.sa")

    }
}

def abortBuild() {
    log.warn("Next Abort Stage URL: ${nextAbortStageURL}")
    log.warn("jenkin status: ${nextAbortStageURL}")
    if(nextAbortStageURL?.toString()!=null) {
//        resumeJenkinBuild = HttpBuilderUtil.postUtil(restURL, nextAbortStageURL?.toString(), restUser, restPass, "", "URLENC")
       def resumeJenkinBuild = HttpRestUtil.JPost(nextAbortStageURL)
        log.warn("Build Approval Request Response : ${resumeJenkinBuild.statusText}")
        if (resumeJenkinBuild?.status != 200) {
            throw new InvalidInputException("Unable to connect Jenkins" +
                    "| Change ID: ${issue?.key} | Change Status: ${changeStatus} | Reason: ${resumeJenkinBuild.statusText} , please try again if error still exist contact techsupport@elm.sa")
        }
    } else{
        throw new InvalidInputException("Unable to Connect Jenkins, received response: 404 - Page not found , please try again if error still exist contact techsupport@elm.sa" +
                "| Change ID: ${issue?.key} | Change Status: ${changeStatus}")
    }
}

def getNextPendingAaction() {
    def restReader
    def restResonse
    log.warn("Next Approval URL: ${jenkinBuildURL}")
//    def getNextApprovalStage = HttpBuilderUtil.getUtil(restURL, jenkinBuildURL.toString(), restUser, restPass, "TEXT")
    def getNextApprovalStage = HttpRestUtil.JGet(jenkinBuildURL)
//    restReader = getNextApprovalStage["reader"]
//    restResonse = getNextApprovalStage["response"].toString()
    restResonse = getNextApprovalStage?.status
    restReader = getNextApprovalStage?.body?.object
    log.warn("NextPending Action Response: ${restResonse}")
    if (restResonse == 200 && restReader) {
        stageId = restReader?.id?.toString()
        nextAbortStageURL = restReader?.abortUrl?.toString()
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