package com.elm.scriptrunner.CdxIntegration.RestEndPoint

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.TransitionOptions
import com.elm.scriptrunner.library.CommonUtil
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript
import groovy.transform.Field

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate


//transitionIssueOnBoard() { MultivaluedMap queryParams ->
            ApplicationUser loggedInUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
            def issueKey = queryParams.getFirst("issueKeys").toString()
            def issueObject = ComponentAccessor.getIssueManager().getIssueByCurrentKey(issueKey)
            def issueService = ComponentAccessor.getIssueService()
            IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
            issueInputParameters.setSkipScreenCheck(true)

            def transitionOptions = new TransitionOptions.Builder()
            
                .build()

            def ACTION_ID = 651
            def transitionValidationResult =
                issueService.validateTransition(loggedInUser, issueObject.id, ACTION_ID, issueInputParameters, transitionOptions)

//            if (transitionValidationResult.isValid()) {
                issueService.transition(loggedInUser, transitionValidationResult).getIssue()

return (transitionValidationResult.errorCollection)
