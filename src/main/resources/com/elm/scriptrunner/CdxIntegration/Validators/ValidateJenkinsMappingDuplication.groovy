package com.elm.scriptrunner.CdxIntegration.Validators


import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.opensymphony.workflow.InvalidInputException


CommonUtil.executeScriptWithAdmin('atlassbot')

def JJMProjectTypeName = CommonUtil.getCustomFieldObject(13708).name.toString()
def JJMProjectPipleLineName = CommonUtil.getCustomFieldObject(13603).name.toString()
def JJPKeyName = CommonUtil.getCustomFieldObject(13608).name.toString()
def JJMProjectType = CommonUtil.getCustomFieldValue(issue,13708)//issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(13708))
def JJMProjectPipleLine = CommonUtil.getCustomFieldValue(issue,13603) //issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(13603))
def JJMProjectKey = CommonUtil.getCustomFieldValue(issue,13608) //issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(13608))
def jql = "'${JJMProjectTypeName}' =  '${JJMProjectType}' and '${JJMProjectPipleLineName}'~'${JJMProjectPipleLine}' and '${JJPKeyName}'~'${JJMProjectKey}'"
def issuesList = CommonUtil.findIssues(jql, Globals.botUser) as List
log.warn(jql)
log.warn("Existing confg list: "+ issuesList)
if (!issuesList.isEmpty()) {
    throw new InvalidInputException("You cannot create duplicate configurations, this configuration already exist: ${issuesList.toString()}")
}