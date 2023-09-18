package com.elm.scriptrunner.library

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.bamboo.BambooApplicationType
import com.atlassian.applinks.api.application.bitbucket.BitbucketApplicationType
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType
import com.atlassian.applinks.api.application.crowd.CrowdApplicationType
import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.Response
import com.atlassian.sal.api.net.ResponseException
import com.atlassian.sal.api.net.ResponseHandler
import groovy.util.logging.Log4j


@Log4j
class DoRequestCall {

    static def doCall(String requestBody, def appType, def restCallURL, Request.MethodType methodType) {
        def appLinkService = ComponentLocator.getComponent(ApplicationLinkService)
        def appLinkName
        switch (appType) {
            case "Bitbucket": appLinkName = appLinkService.getPrimaryApplicationLink(BitbucketApplicationType)
                break
            case "Bamboo": appLinkName = appLinkService.getPrimaryApplicationLink(BambooApplicationType)
                break
            case "Wiki": appLinkName = appLinkService.getPrimaryApplicationLink(ConfluenceApplicationType)
                break
        }
        // We must have a working application link set up to proceed
        assert appLinkName
        def authenticatedRequestFactory = appLinkName.createAuthenticatedRequestFactory()
        if (methodType == Request.MethodType.PUT) {
            authenticatedRequestFactory.createRequest(Request.MethodType.PUT, restCallURL.toString())
                .addHeader("Content-Type", "application/json")
                .setRequestBody(requestBody)
                .execute(responseHandler)
        } else if (methodType == Request.MethodType.GET) {
            authenticatedRequestFactory.createRequest(Request.MethodType.GET, restCallURL.toString())
                .addHeader("Content-Type", "application/json")
                .execute()
        } else if (methodType == Request.MethodType.POST) {
            authenticatedRequestFactory.createRequest(Request.MethodType.POST, restCallURL.toString())
                .addHeader("Content-Type", "application/json")
                .setRequestBody(requestBody)
                .execute(responseHandler)
        }
    }

    static def wikiPermissionRestCall(String restCallURL) {
        def appLinkService = ComponentLocator.getComponent(ApplicationLinkService)
        def appLinkName = appLinkService.getPrimaryApplicationLink(ConfluenceApplicationType)
        def authenticatedRequestFactory = appLinkName.createAuthenticatedRequestFactory()
        assert appLinkName
        authenticatedRequestFactory.createRequest(Request.MethodType.PUT, restCallURL)
            .addHeader("Content-Type", "application/json")
            .execute(responseHandler)
    }

    static def getRestCall(def appType, def restCallURL) {
        def authenticatedRequestFactory = getAppLinkObject(appType).createAuthenticatedRequestFactory()
        authenticatedRequestFactory.createRequest(Request.MethodType.GET, restCallURL.toString())
            .addHeader("Content-Type", "application/json")
            .execute()
    }

    static def putRestCall(String requestBody, def appType, def restCallURL) {
        def authenticatedRequestFactory = getAppLinkObject(appType).createAuthenticatedRequestFactory()
        authenticatedRequestFactory.createRequest(Request.MethodType.PUT, restCallURL.toString())
            .addHeader("Content-Type", "application/json")
            .setRequestBody(requestBody)
            .execute(responseHandler)
    }

    static def postRestCall(String requestBody, def appType, def restCallURL) {
        def authenticatedRequestFactory = getAppLinkObject(appType).createAuthenticatedRequestFactory()
        authenticatedRequestFactory.createRequest(Request.MethodType.POST, restCallURL.toString())
            .addHeader("Content-Type", "application/json")
            .setRequestBody(requestBody)
            .execute(responseHandler)
    }

    static def deleteRestCall(def appType, def restCallURL) {
        def authenticatedRequestFactory = getAppLinkObject(appType).createAuthenticatedRequestFactory()
        authenticatedRequestFactory.createRequest(Request.MethodType.DELETE, restCallURL.toString())
            .addHeader("Content-Type", "application/json")
            .execute(responseHandler)
    }


    static def getAppLinkObject(def appType) {
        def appLinkService = ComponentLocator.getComponent(ApplicationLinkService)
        def appLinkName
        switch (appType) {
            case "Bitbucket": appLinkName = appLinkService.getPrimaryApplicationLink(BitbucketApplicationType)
                break
            case "Bamboo": appLinkName = appLinkService.getPrimaryApplicationLink(BambooApplicationType)
                break
            case "Wiki": appLinkName = appLinkService.getPrimaryApplicationLink(ConfluenceApplicationType)
                break
            case "crowd":appLinkName = appLinkService.getPrimaryApplicationLink(CrowdApplicationType)
                break
        }
        // We must have a working application link set up to proceed
        assert appLinkName
        return appLinkName
    }

    def static responseHandler = new ResponseHandler<Response>() {
        @Override
        void handle(Response response) throws ResponseException {
//            def intRange = 200..220
            if (![200, 201, 203, 204, 205, 304].contains(response.statusCode)) {
                log.warn(response.responseBodyAsString)
                //def respData = new JsonSlurper().parseText(response.responseBodyAsString)
            } else{
                log.warn(response.responseBodyAsString)
                //def respError = new JsonSlurper().parseText(response.responseBodyAsString)
            }
        }
    }
}
