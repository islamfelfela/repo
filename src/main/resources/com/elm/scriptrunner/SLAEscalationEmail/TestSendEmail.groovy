package com.elm.scriptrunner.SLAEscalationEmail

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.user.ApplicationUser


//def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("CDXL-38")

// def ChangeStart = getFieldByName("Change Start Date / Time").getValue()
// def ChangeEnd = getFieldByName("Change End Date / Time").getValue()
// log.warn("No issues found with ${ChangeEnd} ${ChangeStart}")

def issueListForEscalation  = CommonUtil.findIssues(""" slaFunction > remainingPercentage(50,"SecuritySLA") """, Globals.botUser)

def groupedList = issueListForEscalation.groupBy {it.assignee}
//log.warn(groupedList)
//log.warn(issueListForEscalation.groupBy {it.assignee.username})

//log.warn(issueListForEscalation)

groupedList.each{
    //log.warn('group list item ' + it)

    def issueManager = ComponentAccessor.getIssueManager()

    def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)
    //log.warn(it.getValue())
    def temp = it

    html.html {
        head {
            style(type: "text/css",
                '''
                    #myTable, th, td{
                    border: 1px solid black;
                    padding: 8px;
                    text-align: left;
                    }
                    
                    #myTable {
                    border-collapse: collapse;
                    }

''')
        }
        body(id: 'mainBody') {
            table(id: "myTable") {
                tr {
                    th("Key")
                    th("Summary")
                    th("Status")
                }
                if(temp!=null){
                    //log.warn('temp ' + temp.getValue())

                    temp.getValue().each{ issueKey ->
                        tr {
                            //td('test')
                            //td('test')
                            //td('test')
                            //log.warn('issueKey' + issueManager.getIssueObject(issueKey.toString()))
                            td {
                                a(href: "$baseUrl/browse/", "${issueKey}")
                            }
                            td(issueManager.getIssueObject(issueKey.toString()))
                            td(issueManager.getIssueObject(issueKey.toString()).summary.toString())
                            td(issueManager.getIssueObject(issueKey.toString()).status.getName().toString())
                        }
                    }
                }
            }
        }
    }
//log.warn(html)

    String finalHTML = writer.toString()
//    log.warn("Resulting HTML=")
//    log.warn(finalHTML)



}



// def reporterObject = CommonUtil.getInsightAtrributeValueSpecificObject(issue,"""objectType = "Service" AND "Name" = "${issue.assignee.username}" """,1,'Manager')

def findIssues(String jqlSearch, ApplicationUser searcher) {
    def searchService = ComponentAccessor.getComponent(SearchService)
    def issueManager = ComponentAccessor.getIssueManager()
    SearchService.ParseResult parseResult = searchService.parseQuery(searcher, jqlSearch)
    def searchResult = searchService.search(searcher, parseResult.getQuery(), PagerFilter.getUnlimitedFilter())
    def results = searchResult.results
    return results
}
