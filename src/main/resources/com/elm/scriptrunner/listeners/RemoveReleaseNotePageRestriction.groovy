package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonSlurper
import groovy.transform.Field


@Field pageId
@Field getPageInfo
@Field wiki = 'Wiki'

def mainMethod() {
    //Run script with admin user
    def cFClassification = CommonUtil.getCustomFieldValue(issue,12010).toString()
    def runWithAdmin =  ComponentAccessor.getJiraAuthenticationContext().setLoggedInUser(CommonUtil.executeScriptWithAdmin())
    def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(),"UTF-8")
    def pKey =  CommonUtil.getProjectKey(issue)
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn(wikiKey)
    getPageInfo =DoRequestCall.getRestCall(wiki , "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")
    pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)

    DoRequestCall.deleteRestCall(wiki, "rest/experimental/content/${pageId}/restriction")

}

mainMethod()


