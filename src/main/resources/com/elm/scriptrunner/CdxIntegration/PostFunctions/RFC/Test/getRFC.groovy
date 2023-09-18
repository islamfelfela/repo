package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.Test

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.Field
import groovyx.net.http.ContentType
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import com.elm.scriptrunner.library.Constants


@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("ESMS-258")


def mainMethod() {
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()

    def resp = SMGet("/SM/9/rest/changes/${cFRFCNumber}")
    return (resp.body?.object?.Change)
}

static def SMGet(String path) {
    String restURL = Constants.smUrl //"http://192.168.47.157:13083"
    String restUser = Constants.smUsername //"Ingjira"
    String restPass = Constants.smPassword //"ELMhpsm@123"
    def authString = HttpRestUtil.getAuthString(restUser,restPass)

    HttpResponse jsonResponse =
        Unirest.get(restURL+path)
            .header('Authorization', "Basic ${authString}")
            .header('Content-Type', ContentType.JSON as String)
            .asJson()
    return jsonResponse
}
mainMethod()
