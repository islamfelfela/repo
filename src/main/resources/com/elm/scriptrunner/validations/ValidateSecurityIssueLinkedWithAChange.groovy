package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.opensymphony.workflow.InvalidInputException

def mainMethod() {
    def pKey = issue.projectObject.key
    def fixVersion = issue.fixVersions.first()
    log.warn(fixVersion)
    def jqlSearchForVersionRelatedIssues = "project=\"${pKey}\" AND fixVersion=${fixVersion} AND (type = Change AND Classification = Production) AND Status in (Done,'Awaiting Release','Awaiting Stage Deployment','Awaiting Production Deployment')) "
    log.warn(jqlSearchForVersionRelatedIssues)
    def issues = CommonUtil.findIssues(jqlSearchForVersionRelatedIssues, Globals.botUser)
    log.warn(issues)
    if(issue.resolution.name in ['Resolved,Done']) {
        if (issues.isEmpty()) {
            throw (new InvalidInputException("Please be informed that this Security Bug not linked with a production Change  ,Please Send Back to Development"))
        }
    }
}

mainMethod()