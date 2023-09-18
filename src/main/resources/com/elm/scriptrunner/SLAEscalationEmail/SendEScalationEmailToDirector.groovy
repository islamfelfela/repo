package com.elm.scriptrunner.SLAEscalationEmail

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import com.atlassian.jira.web.bean.PagerFilter
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.user.ApplicationUser


def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("MOAZA-89")


def cFieldValue = CommonUtil.getCustomFieldValue(issue,15904) //15856
log.warn(cFieldValue)

def issueListForEscalation  = CommonUtil.findIssues(""" slaFunction > remainingPercentage(50,"SecuritySLA") """, Globals.botUser)
def groupedList = issueListForEscalation.groupBy {it.assignee}

groupedList.each { groupList ->

    def issueManager = ComponentAccessor.getIssueManager()
    def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)
    //log.warn(it.getValue())
    //def temp = it

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
                    th("Project")
                    th("Summary")
                    th("Status")
                    th("Priority")
                    th("SLA Breached In")

                }
                if (groupList != null) {
                    //log.warn('temp ' + temp.getValue())

                    groupList.getValue().each { issueKey ->
                        def securitySLA = CommonUtil.getCustomFieldValue(issueManager.getIssueObject(issueKey.toString()),15904)
                        def issueObject = issueManager.getIssueObject(issueKey.toString())
                        tr {
                            td(href: "$baseUrl/browse/", "${issueKey}")
                            td(issueObject.projectObject.name)
                            td(issueObject.summary)
                            td(issueObject.status.getName())
                            td(issueObject.priority.name)
                            td(issueObject.assignee.name)
                            if(securitySLA >= new Date()){
                            td("""<p style="background-color: #ff0000;  color: #ffffff ">securitySLA</p>""")
                            }
                        }
                    }
                }
            }
        }
    }

//log.warn(html)


    String finalHTML = writer.toString()

    if (groupList.key != null) {
        SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', 'mmojahed@elm.sa','', "SLAs Automation & Escalation Service", finalHTML)
    }
}
