package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


//CommonUtil.executeScriptWithAdmin("bot")

/**
 * a post function that creates a Penetration Request if the Change is Delivered and Penetration Test is Required
 */

def cFClassification = CommonUtil.getCustomFieldValue(issue, 12010)?.toString()
def cFSubcategory = CommonUtil.getCustomFieldValue(issue, 12009)?.toString()
def jqlSearch = "project=${issue.getProjectObject().key} AND type = Penetration-Request AND statusCategory != Done"
def deliveredChangeIssues = CommonUtil.findIssues(jqlSearch, Globals.botUser)
log.warn("Change ID ${issue} ,  cFClassification: ${cFClassification}, cFSubcategory: ${cFSubcategory} ")

def isPenTestRequiredCF = CommonUtil.getCustomFieldValue(issue,15874).toString()
if(isPenTestRequiredCF == 'Yes') {
    if (!deliveredChangeIssues) {
        createIssue()
    } else {
        deliveredChangeIssues.each {
            def versionManager = ComponentAccessor.getVersionManager()
            def versions = versionManager.getVersionsByName(issue.fixVersions.last().name)
            it.setFixVersions(versions)
            ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, it, EventDispatchOption.ISSUE_UPDATED, false)
        }
    }
}

def createIssue() {
    IssueService issueService = ComponentAccessor.getIssueService()
//    def securityUser = ComponentAccessor.getUserManager().getUserByKey("ssikkandar")
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters
            .setProjectId(issue.getProjectObject().getId())
            .setSummary('Auto_Triggered_PenRequest ' + issue.getSummary()?.toString() +" "+ issue.getFixVersions()?.first()?.toString())
            .setDescription("Request to Perform Penteration Test for  " + issue.getFixVersions()?.first())
            .setIssueTypeId('10403')
            .setReporterId(issue.reporterId)
            .setFixVersionIds(issue.getFixVersions()?.first()?.id)
    ApplicationUser user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
    IssueService.CreateValidationResult createValidationResult =
            issueService.validateCreate(user, issueInputParameters)
    if (createValidationResult.isValid()) {
        IssueService.IssueResult createResult = issueService.create(
                user, createValidationResult)
        log.warn(createResult)
        if (!createResult.isValid()) {
            log.warn("Error while creating the issue." + createResult.errorCollection)
        }
    }
}