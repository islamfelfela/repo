package com.onresolve.jira.groovy.jql

import com.atlassian.jira.JiraDataType
import com.atlassian.jira.JiraDataTypes
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.jql.operand.QueryLiteral
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.permission.ProjectPermissions
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand

class BusinessTeamProjects extends AbstractScriptedJqlFunction implements JqlFunction  {
    def projectManager = ComponentAccessor.getProjectManager()
    def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    def permissionManager = ComponentAccessor.getPermissionManager()

    @Override
    String getDescription() {
        "Get List Of Projects which  Business Manager Roles exist "
    }

    @Override
    List<Map> getArguments() {
        Collections.EMPTY_LIST
    }
    @Override
    String getFunctionName() {
        "BusinessTeamProjects"
    }
    @Override
    JiraDataType getDataType() {
        JiraDataTypes.PROJECT
    }
    @Override
    List<QueryLiteral> getValues(
        QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {
        def projectRole = projectRoleManager.getProjectRole('Business Manager')
        projectManager.getProjects().findAll(){
            !projectRoleManager.getProjectRoleActors(projectRole,it).roleActors.isEmpty()
        }.collect {
            new QueryLiteral(operand, it.id)
        }
    }
}