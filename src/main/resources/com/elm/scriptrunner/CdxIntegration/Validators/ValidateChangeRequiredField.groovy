package com.elm.scriptrunner.CdxIntegration.Validators

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.opensymphony.workflow.InvalidInputException

def changeStatus = issue.getStatus().name.toString().toLowerCase()

switch (changeStatus) {
    case Globals.Status.awaitingQACertify:
        reqQAFields()
        break
    case Globals.Status.awaitingStageDeploy:
        reqCloseRFCFeidls()
        break
    case Globals.Status.awaitingProdDeploy:
        reqCloseRFCFeidls()
        break

    case Globals.Status.awaitingTechnicalApproval:
        reqTechApprovalFields()
        break

    case Globals.Status.open:
        reqPMFields()
        break
}

log.warn('Status Filed is :  ' + changeStatus)

def reqQAFields(){
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def subCagetogryField =  issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12009)).toString()
    def isPenTestRequiredCF = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15874)).toString()
    def riskField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(13300)).toString()
    def riskSummaryField =issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12800)).toString()

    log.warn("SubCategory Filed is : '${subCagetogryField}'")
    log.warn("Risk Filed is : '${riskField}'")
    log.warn("Summary Filed is : '${riskSummaryField}'")

    if (subCagetogryField == 'null') {
        throw new InvalidInputException("SubCategory is required")
    }

    if (isPenTestRequiredCF == 'null'){
        throw new InvalidInputException("isPenTestRequiredCF  is required")
    }

    if (riskField == 'null'){
        throw new InvalidInputException("Is There Risk & Issue  is required")
    }

    if (riskField == 'Yes') {
        if (!(riskSummaryField)) {
            throw new InvalidInputException("Risks & Issues Description is required")
        }
    }
}

def reqPMFields() {
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def subCagetogryField =  issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12009)).toString()
    def classificationField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12010)).toString()
    def issueDescriptionField =   issue.getDescription().toString().toLowerCase()
    def elmImpactMRF = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14618)).toString()

    log.warn("SubCategory Filed is : '${subCagetogryField}'")
    log.warn("Classification Filed is : '${classificationField}'")
    log.warn('Description Filed is :  ' + issueDescriptionField)
    log.warn('Delivery Content :  ' + elmImpactMRF)


    if (issueDescriptionField == 'null' ) {
        throw new InvalidInputException('description', "Description is required")
    }
    if (subCagetogryField == 'null') {
        throw new InvalidInputException("SubCategory is required")
    }
    if (classificationField == 'null') {
        throw new InvalidInputException("Classification is required")
    }
    if (elmImpactMRF == 'null') {
        throw new InvalidInputException("Delivery Content is required")
    }
}

def reqCloseRFCFeidls(){
    def cFActualStart =issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14601)).toString()
    def cFActualEnd = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14602)).toString()
    def cfIssues = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14603)).toString()
    def cfIssuesDetails = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14604)).toString()
    def cfFailureReason = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(14605)).toString()

    log.warn("Actual Start  : '${cFActualStart}'")
    log.warn("Actual End : '${cFActualEnd}'")
    log.warn("Issues : '${cfIssues}'")
    log.warn("Issue details : '${cfIssuesDetails}'")
    log.warn("Failure Reason : '${cfFailureReason}'")


    if (cFActualStart == null){
        throw new InvalidInputException("Actual Start Field is required")
    }
    if (cFActualEnd == null){
        throw new InvalidInputException("Actual End Field is required")
    }
    if (cfIssues == 'none'){
        throw new InvalidInputException("Actual Start Field is required")
    }

    if (cfIssues == 'Yes') {
        if (!(cfIssuesDetails)) {
            throw new InvalidInputException("Issue Details is required ")
        }
    }

    if(issue.resolution.name == 'Fail'){
        if (!(cfFailureReason)){
            throw new InvalidInputException("Failure Reason is required")
        }
    }
}

def reqTechApprovalFields() {
    def customFieldManager = ComponentAccessor.getCustomFieldManager()
    def outageExistField =  issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15304)).toString()
    def outageStartField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15302)).toString()
    def outageEndField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15303)).toString()
    def targetStartField =   issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15201)).toString()
    def targetEndField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15202)).toString()
    def assignedDeployerField = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(15400)).toString()


    log.warn("outageExist Field  : '${outageExistField}'")
    log.warn("outageStart Field  : '${outageStartField}'")
    log.warn('outageEnd Field is :  ' + outageEndField)
    log.warn('targetStart Field  :  ' + targetStartField)
    log.warn('targetEnd Field :  ' + targetEndField)


    if (outageExistField == 'null') {
        throw new InvalidInputException("Outage Exist ? is required")
    }

    if (targetStartField == 'null') {
        throw new InvalidInputException("Target Start is required")
    }
    if (targetEndField == 'null') {
        throw new InvalidInputException("Target End is required")
    }
    if (issue.assignee.name == 'null') {
        throw new InvalidInputException("assignee is required")
    }
    if (assignedDeployerField == 'null') {
        throw new InvalidInputException("assigned deployer is required")
    }

}
