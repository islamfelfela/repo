package com.elm.scriptrunner.SLAEscalationEmail

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field
import groovy.xml.MarkupBuilder
import java.text.SimpleDateFormat
import org.joda.time.DateTime
import java.text.DateFormat


import java.text.SimpleDateFormat

def issueListForEscalation  = CommonUtil.findIssues(""" type = "Security Bug" and status in (Open ,Development) AND Project = ZAW AND assignee is not EMPTY """, Globals.botUser)
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
                    th("Assignee")
                    th("SLA Breached In")

                }
                if (groupList != null) {
                    //log.warn('temp ' + temp.getValue())

                    groupList.getValue().each { issueKey ->
                        def issueObject = issueManager.getIssueObject(issueKey.toString())
                        def securityDateTimeBreach = CommonUtil.getCustomFieldValue(issueObject, 15904).toString()
                        log.warn(securityDateTimeBreach)
                        def securitySLA = dateTimeFormat(securityDateTimeBreach)
                        tr {
                            td(href: "$baseUrl/browse/", "${issueKey}")
                            td(issueObject.projectObject.name)
                            td(issueObject.summary)
                            td(issueObject.status.getName())
                            td(issueObject.priority.name)
                            td(issueObject.assignee.name)
                            if(securitySLA >= new Date()){
                                td('bgcolor': "#ff0000" , "${securitySLA}")
                            }else{
                                td('bgcolor': "#000000" , "${securitySLA}")
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
        def developerUser = CommonUtil.getInsightObjectByAttributeValue("objectType = Employees AND UserName = ${groupList.key.username}",1)

        def managerUser = CommonUtil.getInsightCFValueSpecificAttribute(developerUser.id,'Manager')
        log.warn(managerUser)
        if(managerUser != 'null'){
            log.warn(managerUser.size())
            def managerUserEMail = managerUser.first()+'@elm.sa'
            log.warn(managerUserEMail)
            SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '','mmojahed@elm.sa', "SLAs Automation & Escalation Service", finalHTML)
        }
    }
}

static def dateTimeFormat(def dateValue){
    SimpleDateFormat format = new SimpleDateFormat("ddd MMM HH:mm:ss yyyy")
    Date date = format.parse(dateValue)
    def strDate =  new Date(date)
    return  new Date(date)
}