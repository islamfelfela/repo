package com.elm.scriptrunner.postfunctions

import com.atlassian.applinks.api.ApplicationLinkResponseHandler
import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.crowd.CrowdApplicationType
import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.ResponseException
import com.atlassian.sal.api.net.ResponseHandler
import groovy.json.JsonBuilder
import com.atlassian.sal.api.net.Response


def addUsersToCrowd() {
    def requestBody = "{\"name\":\"mmojahed\"}"
    def groupParam = [groupname: 'confluence users']
    def restCallURL = '/rest/usermanagement/1/group/user/direct?[groupname="confluence users"'
    def appLinkService = ComponentLocator.getComponent(ApplicationLinkService)
    def appLink = appLinkService.getPrimaryApplicationLink(CrowdApplicationType)
    assert appLink
    def authenticatedRequestFactory = appLink.createAuthenticatedRequestFactory()
try{
    authenticatedRequestFactory.createRequest(Request.MethodType.POST, '/rest/usermanagement/1/group/user/direct')
        .addHeader("Content-Type", "application/json")
        .setRequestBody(requestBody)
//        .addRequestParameters(groupParam)
        .execute( new ApplicationLinkResponseHandler<Boolean>() {

            Boolean handle(Response response) throws ResponseException {
                return response.successful
            }

            Boolean credentialsRequired(Response response) throws ResponseException {
                // the remote server rejected our credentials, so prompt the user to authenticate
                // using the URI provided by AuthorisationURIGenerator.getAuthorisationURI()
                // ...
            }

        })
} catch (ResponseException re) {
    // the request failed to complete normally
}

//    def crowdUser = ["name": reportUser.get(0)]
//    DoRequestCall.addUsersToCrowd(new JsonBuilder(requestBody).toString(), 'crowd', restCallURL, groupParam)
}

addUsersToCrowd()