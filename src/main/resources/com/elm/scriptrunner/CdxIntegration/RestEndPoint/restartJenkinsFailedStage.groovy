package com.elm.scriptrunner.CdxIntegration.RestEndPoint

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.HttpRestUtil
import com.onresolve.scriptrunner.runner.rest.common.CustomEndpointDelegate
import groovy.json.JsonBuilder
import groovy.transform.BaseScript
import javax.ws.rs.core.MultivaluedMap
import javax.ws.rs.core.Response

@BaseScript CustomEndpointDelegate delegate
restartJenkinsFailedStage(httpMethod: "GET") {
    MultivaluedMap queryParams ->
//        try {
//        def issueKey = queryParams.getFirst("issueKey") as String
        def issueId = queryParams.getFirst("issueId") as Long

        log.warn("Received Data :  ${issueId}")
        if (!issueId) {
            return Response.ok(new JsonBuilder(['type':'error','code': '404', "message": "Missing Parameter Jira issue Id"]).toString()).build()
        } else {
            restartBuild(issueId)
        }
        //} catch (e) {
        //  return Response.ok(new JsonBuilder(["code": 500, "Error": e?.message?.toString()]).toString()).build()
        //}
}

def restartBuild(Long issueId) {
    // def restartBuildResults
    def issue = ComponentAccessor.getIssueManager().getIssueObject(issueId)
    def jenkingMapingURL = CommonUtil.getCustomFieldValue(issue, 13709).toString()
    def jenkinsBuildURL = CommonUtil.getCustomFieldValue(issue, 13700)?.toString()
    def buildNumber = jenkinsBuildURL?.split("\\|")?.getAt(0)?.split("\\[")?.getAt(1)
    jenkingMapingURL = jenkingMapingURL?.split(":")
    def urlEncoded = URLEncoder.encode(jenkingMapingURL?.last(), "UTF-8")
    def failedStagesUrl = "/job/${jenkingMapingURL?.first()}/job/${jenkingMapingURL?.getAt(1)}/job/${urlEncoded}/${buildNumber}/restart/api/json"
    def restartBuildUrl = "/job/${jenkingMapingURL?.first()}/job/${jenkingMapingURL?.getAt(1)}/job/${urlEncoded}/${buildNumber}/restart/restart"
    log.warn("rest call: ${Constants.JenkinsUrl}${failedStagesUrl}")
    log.warn("rest call: ${Constants.JenkinsUrl}${restartBuildUrl}")
    try {
        def failedStageResponse = HttpRestUtil.JGet(failedStagesUrl)
        log.warn(failedStageResponse?.body?.object)
        if (failedStageResponse.status == 200 && failedStageResponse?.body?.object?.restartableStages?.size() > 0) {
        log.warn (failedStageResponse?.body?.object?.restartableStages?.toString())
//        if (failedStageResponse?.body?.object?.restartableStages?.size() > 0) {
            //def resp = HttpRestUtil.JPost(restartBuildUrl,["json":"{'stageName': '${failedStageResponse.body.object.restartableStages?.last()}'"])
            //return resp
            def req = HttpRestUtil.JFieldPost(restartBuildUrl,
                ["json": """{"stageName": "${failedStageResponse?.body?.object?.restartableStages?.last()}",
           "Jenkins-Crumb": "3ecfac5d5fb355910b045a71e42541733cfb5008023c1e72cdc762e1fd8bc854"}"""])
            return Response.status(Response.Status.OK).entity((new JsonBuilder(['type':'success','message':"Jenkins Build has been restarted " + req?.body?.object])).toString()).build()
        } else {
            return Response.serverError().entity((new JsonBuilder(['type':'error','message':'there is no restartableStages'])).toString()).build()
        }
    } catch (Exception e) {
        log.warn(e.message)
        return Response.status(Response.Status.NOT_FOUND).entity((new JsonBuilder(['type':'error','message':e])).toString()).build()
    }
}