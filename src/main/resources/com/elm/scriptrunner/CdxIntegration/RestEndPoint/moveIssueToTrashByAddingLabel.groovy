package com.elm.scriptrunner.CdxIntegration.RestEndPoint

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.label.LabelManager
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonOutput
import groovy.transform.Field
import groovy.transform.BaseScript

import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

addTobeDeletedLabel(httpMethod: "GET")
    {
        MultivaluedMap queryParams, String body ->
            CommonUtil.executeScriptWithAdmin("atlassbot")
            def issueId = queryParams.getFirst("issueId") as Long
            def issueObject = ComponentAccessor.getIssueManager().getIssueObject(issueId)
            def labelManager = ComponentAccessor.getComponent(LabelManager)
            labelManager.addLabel(Globals.botUser,issueId,'ToBeDeleted',false)
            def issueService = ComponentAccessor.issueService
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
            issueInputParameters.setSecurityLevelId(10600L)
            def update = issueService.validateUpdate(Globals.botUser, issueId, issueInputParameters)
            if (update.isValid()) {
                def flag = [
                    type : 'success',
                    title: "Success",
                    close: 'auto',
                    body : "Issue moved successfully to trash"
                ]
                issueService.update(Globals.botUser, update)
//                Response.ok(['type':'success','message':'Issue moved successfully to trash']).build()
                Response.ok(JsonOutput.toJson(flag)).build()
            }
            else{
                flag = [
                    type : 'failure',
                    title: "An error occurred",
                    close: 'manual',
                    body : "There was an error trying to move this issue to trash"
                ]
                log.warn('IssueId: ' +issueObject.id + 'Issue Labels: ' + issueObject.labels)
//                Response.ok(['type':'error','message':'Please contact administrator']).build()
                Response.ok(JsonOutput.toJson(flag)).build()

            }
    }