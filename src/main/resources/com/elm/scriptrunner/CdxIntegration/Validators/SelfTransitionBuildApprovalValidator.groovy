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


log.warn("Change ID: ${issue.toString()}")
log.warn("Change Status: ${changeStatus}")
log.warn("Is Change CDX: ${isCdx()}")

if (isCdx()) {
    mainMethod()
}

def mainMethod() {
    getNextPendingAaction()
    log.warn("stage id: " + stageId)
    switch (changeStatus) {
        case Globals.Status.awaitingQACertify.toLowerCase().trim(): //deploy on QA
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.deployOnQA.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Pipeline stage must be '${Globals.JStatus.deployOnQA}' | Current stage is:  ${stageId}")
            }
            break
        case Globals.Status.awaitingStageDeploy.toLowerCase().trim(): //deploy on staging
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.deployOnStaging.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Pipeline stage must be '${Globals.JStatus.deployOnStaging}' | Current stage is:  ${stageId}")
            }
            break
        case Globals.Status.awaitingProdDeploy: // deployment on production
            if (stageId?.toLowerCase()?.trim() == Globals.JStatus.deployOnProd.toLowerCase().trim()) {
                approveBuild()
            } else {
                throw new InvalidInputException("Pipeline stage must be '${Globals.JStatus.deployOnProd}' | Current stage is:  ${stageId}")
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
//        resumeJenkinBuild = HttpBuilderUtil.postUtil(restURL, nextApprovalStageURL?.toString(), restUser, restPass, "", "URLENC")
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
//    def getNextApprovalStage = HttpBuilderUtil.getUtil(restURL, jenkinBuildURL.toString(), restUser, restPass, "TEXT")
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
