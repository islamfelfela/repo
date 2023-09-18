package com.elm.scriptrunner.CdxIntegration.Validators


import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Constants
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
import com.opensymphony.workflow.InvalidInputException

triggerJenkinsBuild()

def triggerJenkinsBuild() {
    CommonUtil.executeScriptWithAdmin("atlassbot")
    def errorMsg1 = "Some error occurred while calling Jenkins, please try again or contact techsupport@elm.sa | Reason:  "
    log.warn("Change Request ID:  " + issue)
    def jenkingMapingURL = CommonUtil.getCustomFieldValue(issue, 13709).toString()
    def fixVersion = issue.getFixVersions()
    fixVersion = !fixVersion.isEmpty() ? fixVersion = fixVersion.first().toString() : ""
    log.warn("Jenkin Pipeline url" + jenkingMapingURL)

    if (jenkingMapingURL != "None" && jenkingMapingURL != "null") {
        jenkingMapingURL = jenkingMapingURL.split("/job/")
        def urlEncoded = URLEncoder.encode(jenkingMapingURL.last(), "UTF-8")
        def uriPath = "/job/${jenkingMapingURL.first()}/job/${urlEncoded}/buildWithParameters"
        def buildData = [
            version     : fixVersion,
            jiraIssueKey: issue.key.toString()
        ]
        log.warn("fix Version: " + fixVersion)
        log.warn("Jenkin Call URL: " + Constants.JenkinsUrl + uriPath)
        def jenkinsCall = HttpRestUtil.JPost(uriPath, buildData)

        log.warn("jenkin response: " + jenkinsCall.status)
        if (jenkinsCall.status != 201) {
            throw new InvalidInputException(errorMsg1 + jenkinsCall.statusText)
        }
    } else {
        log.warn("No jenkin url found")
        throw new InvalidInputException(errorMsg1 + "No pipeline found for the change request")
    }
}