package com.elm.scriptrunner.jql

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.jql.parser.JqlQueryParser
import com.atlassian.jira.jql.query.LuceneQueryBuilder
import com.atlassian.jira.jql.query.QueryCreationContext
import com.atlassian.jira.jql.validator.NumberOfArgumentsValidator
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.MessageSet
import com.atlassian.query.clause.TerminalClause
import com.atlassian.query.operand.FunctionOperand
import com.onresolve.jira.groovy.jql.AbstractScriptedJqlFunction
import com.onresolve.jira.groovy.jql.JqlQueryFunction
import org.apache.lucene.search.Query

import java.text.MessageFormat

class ReOpenedIssueCount extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    /**
     * Modify this query as appropriate.
     *
     * See {@link java.text.MessageFormat} for details
     */
    public static final String TEMPLATE_QUERY =
        "project = JRA and fixVersion = {0} and affectedVersion = {0}"

    def queryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def luceneQueryBuilder = ComponentAccessor.getComponent(LuceneQueryBuilder)
    def searchService = ComponentAccessor.getComponent(SearchService)

    @Override
    String getDescription() {
        "Create release notes"
    }

    @Override
    MessageSet validate(ApplicationUser user, FunctionOperand operand, TerminalClause terminalClause) {
        def messageSet = new NumberOfArgumentsValidator(1, 1, getI18n()).validate(operand)

        if (messageSet.hasAnyErrors()) {
            return messageSet
        }

        def query = mergeQuery(operand)
        messageSet = searchService.validateQuery(user, query)
        messageSet
    }

    @Override
    List<Map> getArguments() {
        [
            [
                description: "Version to generate release notes for",
                optional   : false,
            ]
        ]
    }

    @Override
    String getFunctionName() {
        "releaseNotes"
    }

    @Override
    Query getQuery(QueryCreationContext queryCreationContext, FunctionOperand operand, TerminalClause terminalClause) {

        def query = mergeQuery(operand)
        luceneQueryBuilder.createLuceneQuery(queryCreationContext, query.whereClause)
    }

    private com.atlassian.query.Query mergeQuery(FunctionOperand operand) {
        def queryStr = MessageFormat.format(TEMPLATE_QUERY, operand.args.first())
        queryParser.parseQuery(queryStr)
    }
}