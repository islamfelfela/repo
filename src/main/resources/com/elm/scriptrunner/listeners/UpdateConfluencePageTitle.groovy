package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionUpdatedEvent
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Field


@Field wiki = 'Wiki'

def mainMethod () {
  
    def runWithAdmin =  CommonUtil.executeScriptWithAdmin()

    def event = event as VersionUpdatedEvent
    def originalPageId = event.originalVersion.id
    def originalPageTitle = URLEncoder.encode(event.originalVersion.name,"UTF-8")
    def newPageId = event.version.id
    def newPageTitle = event.version.name
    def pKey = event.originalVersion.project.key
    def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn(wikiKey)

    def getPageInfo = DoRequestCall.getRestCall(wiki,"rest/api/content/search?cql=space='${wikiKey}'%20and%20title='${originalPageTitle}'")

    def pageResult = new JsonSlurper().parseText(getPageInfo.toString()).results

    if (pageResult?.id?.get(0)) {
        def pageId = pageResult.id.get(0)
        def getCurrentPageVersion =  DoRequestCall.getRestCall(wiki,"rest/api/content/${pageId}?expand=version")

        def currentPageVersion = new JsonSlurper().parseText(getCurrentPageVersion.toString())?.version?.number
        def getCurrentPageBody = DoRequestCall.getRestCall(wiki,"rest/api/content/${pageId}?expand=body.storage")

        def getCurrentPageContent = new JsonSlurper().parseText(getCurrentPageBody.toString()).body.storage.value.toString()  //.replaceAll("fixversion= ${originalPageId}", "fixversion= ${newPageId}")
        log.warn(pageId)
        log.warn(getCurrentPageVersion)

        def params = [
            id     : pageId,
            type   : "page",
            title  : newPageTitle,
            space  : [
                key: pKey // set the space key - or calculate it from the project or something
            ],
            body   : [
                storage: [
                    value           :
                        getCurrentPageContent
                    , representation: "storage"
                ]
            ],
            version: [
                "number": currentPageVersion + 1
            ]
        ]

        def jsonParam = new JsonBuilder(params).toString()
        def request = DoRequestCall.putRestCall(jsonParam,wiki,"rest/api/content/${pageId}")

    }else{
        log.warn("There No Release Note Page with this Release Name or You might don't have the required permission")
    }
}

mainMethod()
