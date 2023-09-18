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


def issueListForEscalation  = CommonUtil.findIssues(""" type = "Security Bug" AND status in (Open,Development) """, Globals.botUser)
def groupedList = issueListForEscalation.groupBy {it.getCustomFieldValue(it.assignee)}

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
                        tr {
                            td(href: "$baseUrl/browse/", "${issueKey}")
                            td(issueManager.getIssueObject(issueKey.toString()).projectObject.name)
                            td(issueManager.getIssueObject(issueKey.toString()).summary)
                            td(issueManager.getIssueObject(issueKey.toString()).status.getName())
                            td(issueManager.getIssueObject(issueKey.toString()).priority.name)
                            td(CommonUtil.getCustomFieldValue(issueManager.getIssueObject(issueKey.toString()), 15904))
                        }
                    }
                }
            }
        }
    }

//log.warn(html)


    String finalHTML = writer.toString()



    if (groupList.key != null) {
        SendJiraEmail.sendEmailToUsersAnnouncement('mmojahed@elm.sa', '','', "SLAs Automation & Escalation Service", finalHTML)
        log.warn(groupList)
    }
}
