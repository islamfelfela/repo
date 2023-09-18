package com.elm.scriptrunner.CdxIntegration.Validators

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.opensymphony.workflow.InvalidInputException


def mainMethod() {
    def pKey = issue.projectObject.key
    def fixVersion = issue.fixVersions.first()
    log.warn(fixVersion)
    def jqlSearchForVersionRelatedIssues = "project=\"${pKey}\" AND fixVersion=${fixVersion}"
    def jqlSearchUnResolvedIssues = "project = '${pKey}' AND fixVersion = ${fixVersion} AND issuetype in (Bug,Story,Technical-Task,\"Security Bug\") AND status not in (Done,\"Awaiting Release\")"
    def jqlSearchUnResolvedProblems = "project = '${pKey}' AND fixVersion = ${fixVersion} AND type = Problem AND status not in (\"Permanent Solution Provided\",\"Workaround Provided\",Done)"
    def jqlSearchAllReleaseIssues = "project = '${pKey}' AND fixVersion = ${fixVersion} AND issuetype in (Bug,Story,Problem,Technical-Task,Enhancement,\"Security Bug\")"
    def issues =  CommonUtil.findIssues(jqlSearchForVersionRelatedIssues, Globals.botUser)
    def UnresolvedIssues = CommonUtil.findIssues(jqlSearchUnResolvedIssues, Globals.botUser)

    def unReleasedProblems = CommonUtil.findIssues(jqlSearchUnResolvedProblems, Globals.botUser)
    def allReleaseIssues = CommonUtil.findIssues(jqlSearchAllReleaseIssues, Globals.botUser)


    log.warn('UnresolvedIssues: ' + UnresolvedIssues)
    log.warn('unReleasedProblems: ' + unReleasedProblems)
    log.warn('allReleaseIssues: ' + allReleaseIssues)

    issues.each { it ->
        if (allReleaseIssues) {
            if (UnresolvedIssues||unReleasedProblems) {
                throw (new InvalidInputException("You need to take an action (Verify) for all Issues under this Release " +
                    UnresolvedIssues.each { it.key }))
            }
        } else {

            throw (new InvalidInputException("Your Release should contain at least one issue of the following types (Story/Bug/Problem/Security-Bug)"))
        }
    }
}
if (issue.projectObject.key != 'DMS') {
    mainMethod()
}