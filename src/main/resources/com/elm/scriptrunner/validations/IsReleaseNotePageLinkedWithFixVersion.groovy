package com.elm.scriptrunner.validations


import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.ProductsKeyMap
import com.opensymphony.workflow.InvalidInputException
import groovy.json.JsonSlurper
import groovy.transform.Field

def mainMethod () {

    CommonUtil.executeScriptWithAdmin()
    def fixVersion = URLEncoder.encode(issue.fixVersions.last().name.replaceAll("\\s",""), "UTF-8")
    def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()

    if (!isReleaseNotePageExist(wikiKey,fixVersion)){
        throw new InvalidInputException("There is no Release Note Page linked with the specified fixVersion")
    }
}

boolean isReleaseNotePageExist(def wikiKey, def fixVersion){
    def wikiPageRestCall = "rest/api/content/search?cql=space=${wikiKey}%20and%20title='${fixVersion}'"
    def pageId = DoRequestCall.getRestCall('Wiki',wikiPageRestCall)
    return (new JsonSlurper().parseText(pageId.toString()).results.id.size() > 0)
}

mainMethod()