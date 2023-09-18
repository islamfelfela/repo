package com.elm.scriptrunner.CdxIntegration.Conditions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


/**
 * a condition that checks if the issue is a CDX issue without operations team
 */

def mainMethod() {
//    ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")
    def jjmProjectNatureObject = CommonUtil.getCustomFieldObject(13708)?.name?.toString()
    // jenmkins mapping project nature ops or without ops
    def jjmProjectKeyObject = CommonUtil.getCustomFieldObject(13608)?.name?.toString()
    // Project key in jenkins mapping
    def jjmJenkinsBranchObject = CommonUtil.getCustomFieldObject(13603)?.name?.toString()
    // jenkins branch name from mapping project
    def changeProjectKey = CommonUtil.getProjectObjByIssue(issue)?.key?.toString()  // Project Key of the change
    def changeJenkinsBranch = CommonUtil.getCustomFieldValue(issue, 13709)?.toString()?.toLowerCase()
// Jenkin branch from the change
    changeJenkinsBranch = changeJenkinsBranch != "none" ? changeJenkinsBranch.toString() : "none"
    def jqlWithOps = "'${jjmProjectKeyObject}' ~  '${changeProjectKey}' and '${jjmProjectNatureObject}'= 'With Operations'"
    def jqlWithoutOps = "'${jjmProjectKeyObject}' ~  '${changeProjectKey}' and '${jjmProjectNatureObject}'= 'Without Operations'"

    List mappingResultsWithOps = CommonUtil.findIssues(jqlWithOps, Globals.botUser) as List
    List mappingResultsWithoutOps = CommonUtil.findIssues(jqlWithoutOps, Globals.botUser) as List

    log.warn(" Change : ${issue.toString()}")
    log.warn("Jenkins Branch : ${changeJenkinsBranch}")
    log.warn(" JQL With Ops: ${jqlWithOps}")
    log.warn("Mapping results with ops : ${mappingResultsWithOps}")
    log.warn(" JQL without ops : ${jqlWithoutOps}")
    log.warn(" Mapping results without ops: ${mappingResultsWithoutOps}")

//check for selected jenkins branch and project in mapping, if not mapping found it means its non cdx projects and it should enable the send to cab transition.

    if (mappingResultsWithOps.isEmpty() && mappingResultsWithoutOps.isEmpty()) {

        passesCondition = false

    } else {

        passesCondition = !mappingResultsWithoutOps.isEmpty()
    }
}

mainMethod()
