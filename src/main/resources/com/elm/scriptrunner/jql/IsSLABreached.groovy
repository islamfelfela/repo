package com.elm.scriptrunner.jql

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.LuceneQueryBuilder
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.jira.groovy.jql.JqlQueryFunction
import org.apache.lucene.search.Query

class IsSLABreached extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    /**
     * Modify this query as appropriate.
     *
     * See {@link java.text.MessageFormat} for details
     */
    public static final String TEMPLATE_QUERY =
        """type = \"Security Bug\" AND ((priority = Blocker AND  CyberSecruitySLA in time(\">=\", \"48h\")) OR  
        (priority = High AND CyberSecruitySLA in time(\">=\", \"240h\")) OR 
        (priority = Medium AND CyberSecruitySLA in time(\">=\", \"528h\")) OR 
        (priority = Low AND  CyberSecruitySLA in time(\">=\", \"1584h\")))"""

    def queryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def luceneQueryBuilder = ComponentAccessor.getComponent(LuceneQueryBuilder)
    def searchService = ComponentAccessor.getComponent(SearchService)

    @Override
    String getDescription() {
        "Get List Of Breached Security Issues"
    }


    @Override
    List<Map> getArguments() {
        Collections.EMPTY_LIST
    }

    @Override
    String getFunctionName() {
        "BreachedSecurityIssues"
    }

    @Override
    Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        def query = mergeQuery(operand)
        luceneQueryBuilder.createLuceneQuery(queryCreationContext, query.whereClause)
    }

    private com.atlassian.query.Query mergeQuery(FunctionOperand operand) {
        //def queryStr = MessageFormat.format(TEMPLATE_QUERY, operand.args.first())
        queryParser.parseQuery(TEMPLATE_QUERY)
    }
}