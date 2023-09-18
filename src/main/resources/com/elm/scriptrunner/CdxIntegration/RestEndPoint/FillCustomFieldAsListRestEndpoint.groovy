package com.elm.scriptrunner.CdxIntegration.RestEndPoint



import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

getReleaseServiceList(httpMethod: "GET") { MultivaluedMap queryParams ->
    issueId = queryParams.get("issueId")?.getAt(0) as long
    def issueObject = ComponentAccessor.getIssueManager().getIssueObject(issueId)
    String projectKey = issueObject.projectObject.key
    log.warn("Project Key: ${projectKey}")
    def jqlQuery = "'JJM Project Key'  ~ ${projectKey}"
    def serviceList = serviceList(jqlQuery, 13603)
    log.warn("JQL Query: ${jqlQuery} ")
    log.warn("Issues List: ${serviceList}")
    return Response.ok(new JsonBuilder(serviceList).toString()).build()

}

def serviceList(String jqlQry, Long customField) {
    def componentsList = [:]
    def issueList = CommonUtil.findIssues(jqlQry, Globals.botUser)
    componentsList = [

        items: issueList.collect {
            def cValue = CommonUtil.getCustomFieldValue(it, customField)
            [
                value: cValue,
                html : "<span style=\"float: left\">${cValue}</span>",
                label: cValue,
            ]

        } + [value: "None",
             html : "<span style=\"float: left\">None</span>",
             label: "None",
        ]
    ]
    return componentsList
}