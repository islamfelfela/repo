package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.sal.api.net.ResponseException
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonSlurper
import groovy.transform.Field

@Field pageId
@Field getPageInfo
//@Field wiki = 'Wiki'

/**
 * a post function that adds restriction to the Release Note page
 */

def mainMethod() {
    //Run script with admin user
    CommonUtil.executeScriptWithAdmin()

    if (issue.resolution.name == 'Pass') {

        def originalPageTitle = URLEncoder.encode(issue.fixVersions.last().toString(), "UTF-8")
        def pKey = CommonUtil.getProjectKey(issue)
        def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
        log.warn(wikiKey)

        try {
            getPageInfo = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")
            pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)

        } catch (Exception e) {
            log.warn(e)
        }

        if (pageId) {
            try {
                DoRequestCall.putRestCall('', Globals.wiki, "rest/experimental/content/${pageId}/restriction/byOperation/update/user?userName=mmojahed")
            } catch (ResponseException e) {
                log.warn(e)
            }
        }
    } else {
        log.warn("Restriction can't be applied because Dployment is not successfully implemented")
    }
}

mainMethod()