package com.onresolve.jira.groovy.jql

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

class AutomationRequest extends AbstractScriptedJqlFunction implements JqlQueryFunction {

    /**
     * Modify this query as appropriate.
     *
     * See {@link java.text.MessageFormat} for details
     */
    public static final String TEMPLATE_QUERY =
        """issuetype = Technical-Task AND reporter in (aalibrahim, aalzandi, aalmusned, 
        aoalqahtani, aalomary, abalhelal, amaldossari, asalshubaili, asaalharbi, amaljadhee, 
        afarhan, arai, ahaja, bsafi, baalzahrani, balfaadhel, ealodaili, fmaalqahtani, fsalamah, 
        haljadou, halmaleki, hhussein, lvijayakumar, melhafi, mabdullatif, mnooghukajam, mmubeen, 
        maldraihem, mualotaibi, noaltamimi, oalmunajem, oalzogady, PKunapareddy, salbraikan, sgulay, 
        salsarami, ssaminathan, sdevarajulu, smemon, smansoor, tsalti, alalaskar)"""

    def queryParser = ComponentAccessor.getComponent(JqlQueryParser)
    def luceneQueryBuilder = ComponentAccessor.getComponent(LuceneQueryBuilder)
    def searchService = ComponentAccessor.getComponent(SearchService)

    @Override
    String getDescription() {
        "Get List Of Automation Request"
    }


    @Override
    List<Map> getArguments() {
        Collections.EMPTY_LIST
    }

    @Override
    String getFunctionName() {
        "AutomationRequest"
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