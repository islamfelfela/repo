package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonSlurper


/**
 * a post function that removes the page restriction from the release note page
 */
def mainMethod() {
    CommonUtil.executeScriptWithAdmin()
    //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
    def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(), "UTF-8")
    def pKey = CommonUtil.getProjectKey(issue)
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    def getPageInfo = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/search?cql=space=${wikiKey}%20and%20title='${originalPageTitle}'")
    def pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
    def removePageRestriction = "?pageId=${pageId}&user=mmojahed"
    def wikiBaseURL = "/rest/keplerrominfo/refapp/latest/webhooks/removePageRestriction/run$removePageRestriction"
    DoRequestCall.wikiPermissionRestCall(wikiBaseURL)
}

mainMethod()