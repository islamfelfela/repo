package com.onresolve.jira.groovy.jql

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.LuceneQueryBuilder
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.security.JiraAuthenticationContext
import com.atlassian.jira.security.groups.GroupManager
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.jira.groovy.jql.JqlQueryFunction
import org.apache.lucene.search.Query

class UsersInTeamGroups extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    /**
     * Modify this query as appropriate.
     *
     * See {@link java.text.MessageFormat} for details
     */
    public static final def serviceGroupList = ['cit sd','grc_va','dnp-l3','dco_infra','sos_db-l3','sos_app','oe_pa-l3','noc-aa','ssm','iam_cl',
                                                'dco patch mgmt','dco_oss','dco_nw','vapt','grc','grc_va','sos_db','sos_manager','cs_soc','noc-aa','noc_sa-l3',
                                                'sos devops','its-a','sos_platform','epm-team']

    def queryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def luceneQueryBuilder = ComponentAccessor.getComponent(LuceneQueryBuilder)
    def searchService = ComponentAccessor.getComponent(SearchService)
    def getGroupManager = ComponentAccessor.getComponent(GroupManager)
    def jiraAuthenticationContext = ComponentAccessor.getComponent(JiraAuthenticationContext)

    @Override
    String getDescription() {
        "Issues with currentUser() in TeamGroup"
    }


    @Override
    List<Map> getArguments() {
        Collections.EMPTY_LIST
    }

    @Override
    String getFunctionName() {
        "userInTeamGroup"
    }

    @Override
    Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        def query = mergeQuery(operand)
        luceneQueryBuilder.createLuceneQuery(queryCreationContext, query.whereClause)
    }

    private com.atlassian.query.Query mergeQuery(FunctionOperand operand) {
        //def queryStr = MessageFormat.format(TEMPLATE_QUERY, operand.args.first())
        def groupList = []

        def userGroups = getGroupManager.getGroupNamesForUser(jiraAuthenticationContext.loggedInUser).collect {
            if (serviceGroupList.contains(it)) {
                groupList << it
            }
        }
        log.warn(userGroups)
        log.warn(groupList.join(','))

        def String TEMPLATE_QUERY = "project = SR AND TeamGroup in (${"'"+groupList.join("','")+"'"})"
        queryParser.parseQuery(TEMPLATE_QUERY)
    }

}
