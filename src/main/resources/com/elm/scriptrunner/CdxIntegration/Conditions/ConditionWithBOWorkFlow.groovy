package com.elm.scriptrunner.CdxIntegration.Conditions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field


/**
 * a condition that checks if the issue with Business Owner Workflow
 */
def mainMethod() {
//    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("PERP-1583")
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    def projectRole = projectRoleManager.getProjectRole('Business Manager')
    def projectRoleActor = projectRoleManager.getProjectRoleActors(projectRole,issue.projectObject).roleActors
    log.warn(projectRoleActor)

//check If Project contains Project Role 'Business Manager'
    if (projectRoleActor) {
        passesCondition = true
    } else {
        passesCondition = false
    }
}
mainMethod()