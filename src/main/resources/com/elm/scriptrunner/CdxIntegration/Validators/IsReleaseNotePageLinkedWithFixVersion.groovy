package com.elm.scriptrunner.CdxIntegration.Validators

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.ProductsKeyMap
import com.opensymphony.workflow.InvalidInputException
import groovy.json.JsonSlurper


def mainMethod () {

    CommonUtil.executeScriptWithAdmin()
    def fixVersion = URLEncoder.encode(issue.fixVersions.last().name.replaceAll("\\s", ""), "UTF-8")
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
    def deploymentType = CommonUtil.getCustomFieldValue(issue,15301)
    try {
        if (!isReleaseNotePageExist(wikiKey, fixVersion) && deploymentType != 'CDx') {
            throw new InvalidInputException("There is no Release Note Page linked with the specified fixVersion")
        }
    } catch (Exception e) {
        throw new InvalidInputException("There is no Release Note Page because of UnExpected Error " + e.message)
    }
}

boolean isReleaseNotePageExist(def wikiKey, def fixVersion){
    def wikiPageRestCall = "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${fixVersion}'"
    def pageId = DoRequestCall.getRestCall(Globals.wiki,wikiPageRestCall)
    return (new JsonSlurper().parseText(pageId.toString()).results.id.size() > 0)
}

mainMethod()