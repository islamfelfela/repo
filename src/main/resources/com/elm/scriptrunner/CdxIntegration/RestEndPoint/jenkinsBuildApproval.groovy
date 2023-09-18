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
jenkinsBuildApproval(httpMethod: "GET") {
    MultivaluedMap queryParams ->
//        try {
        def issueKey = queryParams.getFirst("issueKey") as String
        log.warn("Received Data :  ${issueKey}")
        if (!issueKey) {
            return Response.ok(new JsonBuilder(["code": 404, "Error": "Missing Parameter Jira issue key"]).toString()).build()
        } else {
            return Response.ok(new JsonBuilder(["code": 200, "results": abroveBuild(issueKey)]).toString()).build()
        }
        //} catch (e) {
        //  return Response.ok(new JsonBuilder(["code": 500, "Error": e?.message?.toString()]).toString()).build()
        //}
}

def abroveBuild(String issueKey) {
    // def restartBuildResults
    def issue = ComponentAccessor.issueManager.getIssueByCurrentKey(issueKey)
    def jenkingMapingURL = CommonUtil.getCustomFieldValue(issue, 13709).toString()
    def jenkinsBuildURL = CommonUtil.getCustomFieldValue(issue, 13700)?.toString()
    def buildNumber = jenkinsBuildURL?.split("\\|")?.getAt(0)?.split("\\[")?.getAt(1)
    jenkingMapingURL = jenkingMapingURL?.split(":")
    def urlEncoded = URLEncoder.encode(jenkingMapingURL?.last(), "UTF-8")
    def failedStagesUrl = "/job/${jenkingMapingURL?.first()}/job/${jenkingMapingURL?.getAt(1)}/job/${urlEncoded}/${buildNumber}/restart/api/json"
    def restartBuildUrl = "/job/${jenkingMapingURL?.first()}/job/${jenkingMapingURL?.getAt(1)}/job/${urlEncoded}/${buildNumber}/restart/restart"
    log.warn("rest call: ${Constants.JenkinsUrl}${failedStagesUrl}")
    def failedStageResponse = HttpRestUtil.JGet(failedStagesUrl)
    log.warn(failedStageResponse?.body?.object)
    //if (failedStageResponse.status == 302 && failedStageResponse?.body?.object?.restartableStages.size() > 0) {

    //def resp = HttpRestUtil.JPost(restartBuildUrl,["json":"{'stageName': '${failedStageResponse.body.object.restartableStages?.last()}'"])
    //return resp
    return (HttpRestUtil.JFieldPost(restartBuildUrl,
        ["json": """{"stageName": "${failedStageResponse?.body?.object?.restartableStages?.last()}",
           "Jenkins-Crumb": "3ecfac5d5fb355910b045a71e42541733cfb5008023c1e72cdc762e1fd8bc854"}"""]))
}