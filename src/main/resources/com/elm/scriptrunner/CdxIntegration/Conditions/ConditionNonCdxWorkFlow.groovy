package com.elm.scriptrunner.CdxIntegration.Conditions

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


/**
 * a condition that checks if the issue is a non CDX issue
 */

//@Field ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")
@Field def changeProject = CommonUtil.getProjectObjByIssue(issue).key.toString()
@Field def JJPKeyName = CommonUtil.getCustomFieldObject(13608).name.toString()
@Field def jenkinBranchName = CommonUtil.getCustomFieldObject(13603).name.toString()
@Field def jenkinBranchValue = CommonUtil.getCustomFieldValue(issue, 13709).toString().toLowerCase()
@Field changeStatus = issue.getStatus().name.toString().toLowerCase()

def jql = "'${JJPKeyName}' ~  '${changeProject}'"

log.warn(" Change: ${issue.toString()}")
log.warn("Change Status: ${changeStatus}")
log.warn("JQL Query 1: ${jql}")
log.warn("Change Project : ${changeProject}")
log.warn("Jenkin Pipeline : ${jenkinBranchValue}")
log.warn("Conditition Status : ${(jenkinBranchValue == "null" || jenkinBranchValue == "none")}")


switch (changeStatus ){
    case "open":
        CommonUtil.isCdx(jql)
        break
    default:
        if (jenkinBranchValue != "null") {
            passesCondition = jenkinBranchValue == "none"
        } else {
            passesCondition = true

        }
}

def isCdx(jql) {
    List mappingResults = CommonUtil.findIssues(jql, Globals.botUser) as List
    log.warn("Mapping Results : ${mappingResults}")
    log.warn("Is Cdx: ${mappingResults.isEmpty()}" )
    passesCondition = mappingResults.isEmpty()
}