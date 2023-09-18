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
    case Globals.Status.open:
        reqPMFields()
        break
}

log.warn('Status Filed is :  ' + changeStatus)

def reqQAFields(){
    def riskField = CommonUtil.getCustomFieldValue(issue,12800)
    def riskSummaryField = CommonUtil.getCustomFieldValue(issue,13300)
    log.warn("Risk Filed is : '${riskField}'")
    log.warn("Summary Filed is : '${riskSummaryField}'")
    if (riskField == 'None'){
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
//    def subCagetogryField = CommonUtil.getCustomFieldValue(issue,12009)
    def subCagetogryField =  issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(12009)).toString()
//    def classificationField = CommonUtil.getCustomFieldValue(issue,12010)
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
