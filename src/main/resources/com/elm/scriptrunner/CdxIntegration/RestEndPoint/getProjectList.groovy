package com.elm.scriptrunner.CdxIntegration.RestEndPoint

import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate
getProjectlist(httpMethod: "GET") { MultivaluedMap queryParams ->
    def projectManager = ComponentAccessor.getProjectManager()
    def query = queryParams.getFirst("query") as String
    def projectList = [:]
    if(query){
        projectList = [
            items: projectManager.getProjectObjects().findAll { it.projectCategory?.name in ['TDS', 'PS','InternalProjects'] }
            .findAll { it.name.toLowerCase().contains(query.toLowerCase()) }
                .collect {
                    def pName = it.name
                    [
                        value: pName,
                        html : "<span style=\"float: left\">${pName}</span>",
                        label: pName,
                    ]
                }]
    }
    else {
        projectList = [
            items: projectManager.getProjectObjects().findAll { it.projectCategory?.name in ['TDS', 'PS','InternalProjects'] }
                .collect {
                    def pName = it.name
                    [
                        value: pName,
                        html : "<span style=\"float: left\">${pName}</span>",
                        label: pName,
                    ]
                }]
    }
    return Response.ok(new JsonBuilder(projectList).toString()).build()
}