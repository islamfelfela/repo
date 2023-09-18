package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

import groovy.transform.Field

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("WSJ-919")

def mainMethod() {
    //def jqlQuery = "type = Change AND status in (\"Awaiting Release\", Scheduling) AND \"Change number\" is not EMPTY AND \"Deployment Type\" is EMPTY"
    //def issues = CommonUtil.findIssues(jqlQuery, Globals.botUser)

    def linkedPainPoints =  ComponentAccessor.issueLinkManager.getInwardLinks(issue.id).find {
        it.sourceObject.projectObject.key == 'CPP' && it.sourceObject.status.name.toLowerCase() in ['in progress', 'scheduled', 'awaiting business approval']
    }
    def issueStatus = issue.status.name.toLowerCase()
    linkedPainPoints.each {
        log.warn(it.sourceObject.key)
        sourceIssueStatus = it.sourceObject.status.name.toLowerCase()
        if (issueStatus == "development" && sourceIssueStatus != "in progress") {
            CommonUtil.transitionIssue(Globals.botUser, it.sourceObject.id, 221)

        } else if (issueStatus == "awaiting business approval" && sourceIssueStatus != "awaiting business approval") {
            CommonUtil.transitionIssue(Globals.botUser, it.sourceObject.id, 211)

        } else if (issueStatus == "done" && sourceIssueStatus != "done") {
            CommonUtil.transitionIssue(Globals.botUser, it.sourceObject.id, 171)

        }
    }
}

mainMethod()
