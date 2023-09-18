package com.elm.scriptrunner.PainPointsWorkflow.Conditions

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CDXI-4")
//@Field ApplicationUser botUser = ComponentAccessor.getUserManager().getUserByName("bot")

//@Field def changeProject = CommonUtil.getProjectObjByIssue(issue).key.toString()
//def jqlQuery = "project = '${changeProject}' AND issueFunction in linkedIssuesOf('project = CPP')"
//def jqlSearchData = CommonUtil.findIssues(jqlQuery,Globals.botUser)
//
//log.warn("Change Project : ${changeProject}")
//log.warn("jql :  ${jqlSearchData}")
//
//passesCondition = jqlSearchData.size() > 0

def labels = issue.getLabels()
passesCondition = labels.contains('CustomerPainPoint')
