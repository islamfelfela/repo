package com.elm.scriptrunner.CdxIntegration.Validators

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.opensymphony.workflow.InvalidInputException
import groovy.transform.Field


@Field String changeStatus = issue.getStatus().name.toString().toLowerCase()
@Field jenkinBuildURL = CommonUtil.getCustomFieldValue(issue, 13703)
@Field def getNextApprovalStageResponse
@Field def nextApprovalStageURL
@Field def stageId
@Field changeResolution


log.warn("Change ID: ${issue.toString()}")
log.warn("Change Status: ${changeStatus}")
log.warn("Is Change CDX: ${isCdx()}")

if (isCdx()) {
    mainMethod()
}

def mainMethod() {
    changeResolution = issue.resolution?.name?.toLowerCase()?.toString()

    getNextPendingAaction()
    log.warn("stage id: " + stageId)
    switch (changeStatus) {
        case Globals.Status.awaitingQACertify: //Certify QA Release
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.certifyRelease.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Approval stage must be '${Globals.JStatus.certifyRelease}' | Current stage is:  ${stageId}")
            }
            break
//        case Globals.Status.awaitingPmApproval: // send build to CAB
        case Globals.Status.scheduling: // send build to CAB
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.scheduling.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Approval stage must be '${Globals.JStatus.scheduling}' | Current stage is:  ${stageId}")
            }
            break
        case Globals.Status.awaitingStageDeploy: //staging sanity approval
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.stagingCheck.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Approval stage must be '${Globals.JStatus.stagingCheck}' | Current stage is:  ${stageId}")
            }
            break
        case Globals.Status.awaitingProdDeploy: //Process the Production Sanity check
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.ProdCheck.toLowerCase().trim() && changeResolution == "pass") {
                approveBuild()
            } else {
                throw new InvalidInputException("Approval stage must be '${Globals.JStatus.ProdCheck}' | Current stage is:  ${stageId} and resolution you selected is ${changeResolution}")
            }
            break

        default:
            throw new InvalidInputException("You are trying to execute the status other than the allowed one, please contact techsupport@elm.sa")

    }
}

def approveBuild() {
    def resumeJenkinBuild
    log.warn("Next Approval Stage URL: ${Constants.JenkinsUrl + nextApprovalStageURL}")
    log.warn("jenkin status: ${nextApprovalStageURL}")
    if(nextApprovalStageURL?.toString()!=null) {
        resumeJenkinBuild = HttpRestUtil.JPost(nextApprovalStageURL?.toString())
        log.warn("Build Approval Request Response : ${resumeJenkinBuild.statusText}")
        if (resumeJenkinBuild?.status != 200) {
            throw new InvalidInputException("Some error occurred while on approval stage, please try again if error still exist contact techsupport@elm.sa" +
                    "| Change ID: ${issue?.key} | Change Status: ${changeStatus} | Reason: ${resumeJenkinBuild.statusText}")
        }
    } else{
        throw new InvalidInputException("Unable to Connect Jenkins, received response: 404 - Page not found , Please try again" +
                "| Change ID: ${issue?.key} | Change Status: ${changeStatus}")
    }
}

def getNextPendingAaction() {
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
