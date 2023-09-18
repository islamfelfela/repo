//package com.elm.scriptrunner.jql
//
//import com.atlassian.jira.JiraDataType
//import com.atlassian.jira.JiraDataTypes
//import com.atlassian.jira.component.ComponentAccessor
//import com.atlassian.jira.jql.operand.QueryLiteral
//import com.atlassian.jira.jql.query.QueryCreationContext
//import com.atlassian.jira.permission.ProjectPermissions
//import com.atlassian.jira.project.version.VersionManager
//import com.atlassian.query.clause.TerminalClause
//import com.atlassian.query.operand.FunctionOperand
//import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
//import com.onresolve.jira.groovy.jql.JqlQueryFunction
//
//class VersionIsStarted extends AbstractScriptedJqlFunction implements JqlQueryFunction {
//
//    def versionManager = ComponentAccessor.getComponent(VersionManager)
//    def permissionManager = ComponentAccessor.getPermissionManager()
//
//    @Override
//    String getDescription() {
//        "Issues with fixVersion started but not released"
//    }
//
//    @Override
//    List<Map> getArguments() {
//        Collections.EMPTY_LIST
//    }
//
//    @Override
//    String getFunctionName() {
//        "versionsStarted"
//    }
//
//    @Override
//    JiraDataType getDataType() {
//        JiraDataTypes.VERSION
//    }
//
//    @Override
//    List<QueryLiteral> getValues(
//        QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {
//
//        def now = new Date()
//        versionManager.allVersions.findAll {
//            def startDate = it.startDate
//
//            !it.released && startDate && startDate < now
//        }.findAll {
//            queryCreationContext.securityOverriden || permissionManager.hasPermission(ProjectPermissions.BROWSE_PROJECTS, it.project, queryCreationContext.applicationUser)
//        }.collect {
//            new QueryLiteral(operand, it.id)
//        }
//    }
//}