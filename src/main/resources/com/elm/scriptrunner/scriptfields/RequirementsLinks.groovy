package com.elm.scriptrunner.scriptfields

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.RemoteIssueLinkManager

//issue = ComponentAccessor.issueManager.getIssueByCurrentKey("KEFQ-482")

def projectManager = ComponentAccessor.getProjectManager()
def projectCategory = projectManager.getProjectCategoryForProject(projectManager.getProjectObjByName(issue.getProjectObject().getName().toString()))
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def reqLink = issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(13004))
def remoteIssueLinkManager = ComponentAccessor.getComponent(RemoteIssueLinkManager)
def remoteLinks = remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue)
def reqLinks = reqLink + remoteLinks + reqLink
def pKey = issue.getProjectObject().getKey().toString()
def pCategoryID = projectCategory.getId().toString()


return reqLinks.size()
