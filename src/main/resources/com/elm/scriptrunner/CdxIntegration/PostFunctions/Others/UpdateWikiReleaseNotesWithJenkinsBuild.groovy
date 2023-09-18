package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper


/**
 * a post function that updates the wiki page with the jenkins build url
 */

def mainMethod () {

    //Run script with admin user
    CommonUtil.executeScriptWithAdmin()
    log.warn("Change: ${issue.toString()}")

    def originalPageTitle = URLEncoder.encode(issue.fixVersions?.first()?.name?.toString(), "UTF-8")
    def pKey = issue.getProjectObject().key.toString()
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn("Wiki Project Key: ${wikiKey}")
    def getPageInfo = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")
    def pageResult = new JsonSlurper().parseText(getPageInfo.toString()).results
    if (pageResult?.id?.get(0)) {
        def pageId = pageResult.id.get(0)
        def getCurrentPageVersion = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/${pageId}?expand=version")
        def currentPageVersion = new JsonSlurper().parseText(getCurrentPageVersion.toString())?.version?.number
        def getCurrentPageBody = DoRequestCall.getRestCall(Globals.wiki, "rest/api/content/${pageId}?expand=body.storage")
        def getCurrentPageContent = new JsonSlurper().parseText(getCurrentPageBody.toString()).body.storage.value.toString()
        //.replaceAll("fixversion= ${originalPageId}", "fixversion= ${newPageId}")
        def jenkinsBuildURL = CommonUtil.getCustomFieldValue(issue, 13700)?.toString()
        def jenkinsBuildSplit = jenkinsBuildURL?.split("[\\s\\|\\]]")?.getAt(1)?.toString()
        def newPageContents = getCurrentPageContent.replace("BuildURL", """<p><a href="${jenkinsBuildSplit}">BuildURL</a></p>""")

        def params = [
            id     : pageId,
            type   : "page",
            title  : originalPageTitle,
            space  : [
                key: pKey // set the space key - or calculate it from the project or something
            ],
            body   : [
                storage: [
                    value           :
                        newPageContents
                    , representation: "storage"
                ]
            ],
            version: [
                "number": currentPageVersion + 1
            ]
        ]

        def jsonParam = new JsonBuilder(params).toString()
        def request = DoRequestCall.putRestCall(jsonParam, Globals.wiki, "rest/api/content/${pageId}")

    } else {
        log.warn("There No Release Note Page with this Release Name or You might don't have the required permission")
    }
}

mainMethod()