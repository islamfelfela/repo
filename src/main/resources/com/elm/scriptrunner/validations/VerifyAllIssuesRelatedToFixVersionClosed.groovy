package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException

def mainMethod() {
    def userManager = ComponentAccessor.getUserManager()
    def user = userManager.getUserByName("mmojahed")
    def pKey = issue.projectObject.key
    def fixVersion = issue.fixVersions.first()
    log.warn(fixVersion)
    def jqlSearchForVersionRelatedIssues = "project=\"${pKey}\" AND fixVersion=${fixVersion}"
    def jqlSearchUnResolvedIssues = "project = ${pKey} AND fixVersion = ${fixVersion} AND issuetype in (Bug,Story) AND status not in (Done,\"Awaiting Release\")"
    def jqlSearchUnResolvedProblems = "project = ${pKey} AND fixVersion = ${fixVersion} AND type = Problem AND status not in (\"Permanent Solution Provided\",\"Workaround Provided\",Done)"
    def jqlSearchAllReleaseIssues = "project = ${pKey} AND fixVersion = ${fixVersion} AND issuetype in (Bug,Story,Problem,Technical-Task)"
    def issues = CommonUtil.findIssues(jqlSearchForVersionRelatedIssues, user)
    def UnresolvedIssues = CommonUtil.findIssues(jqlSearchUnResolvedIssues, user)
    log.warn(UnresolvedIssues)

    def unReleasedProblems = CommonUtil.findIssues(jqlSearchUnResolvedProblems, user)
    def allReleaseIssues = CommonUtil.findIssues(jqlSearchAllReleaseIssues, user)

    issues.each { it ->
        if (allReleaseIssues) {
            if (UnresolvedIssues||unReleasedProblems) {
                throw (new InvalidInputException("You need to take an action (Verify) for all Issues under this Release "
                    + UnresolvedIssues.each { it.key }))
            }
        } else {
            throw (new InvalidInputException("Your Release should contain at least one issue of the following types (Story/Bug/Problem)"))
        }
    }
}

mainMethod()