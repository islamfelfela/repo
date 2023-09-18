package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.bc.projectroles.ProjectRoleService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleActor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.SimpleErrorCollection
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

@Field issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SUP-6574")


@Field requestType
@Field List<String> excludedProjectCategory = ["Archived", "Support"]
def projectManager = ComponentAccessor.getProjectManager()
def projectRoleService = ComponentAccessor.getComponent(ProjectRoleService)
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
def errorCollection = new SimpleErrorCollection()
def projectsList = CommonUtil.getInsightCField(issue,14805,442)