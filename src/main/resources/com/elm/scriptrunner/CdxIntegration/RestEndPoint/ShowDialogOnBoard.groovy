package com.elm.scriptrunner.CdxIntegration.RestEndPoint

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.config.properties.APKeys
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.workflow.TransitionOptions
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.transform.BaseScript

import javax.ws.rs.core.MediaType
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate

showDialogOnBoard() { MultivaluedMap queryParams ->
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

    if (transitionValidationResult.isValid()) {
        issueService.transition(loggedInUser, transitionValidationResult).getIssue()
    }
    else{
        def validationError
    }
//            def baseUrl = ComponentAccessor.getApplicationProperties().getString(APKeys.JIRA_BASEURL)
       def customDialog =  """<section role="dialog" id="sr-dialog" class="aui-layer aui-dialog2 aui-dialog2-medium" aria-hidden="true">
        <header class="aui-dialog2-header">
            <h2 class="aui-dialog2-header-main">Create IMS Checkpoint</h2>
            <a class="aui-dialog2-header-close">
                <span class="aui-icon aui-icon-small aui-iconfont-close-dialog">Close</span>
            </a>
        </header>
        <div class="aui-dialog2-content">
            <div id="container" style="width:px">
                <form id="my-custom-sr-dialog-form" class="aui" action="/rest/scriptrunner/latest/custom/transitionIssueOnBoard">
                    <button id="submit-button" class="aui-button aui-button-primary">Confirm</button>
                </form>
            </div>
        </div>
        <footer class="aui-dialog2-footer">
            <div class="aui-dialog2-footer-actions">
                <button id="dialog-close-button" class="aui-button aui-button-link">Close</button>
            </div>
        </footer>
        </section>"""

     Response.ok().type(MediaType.TEXT_HTML).entity(customDialog.toString()).build()
    }