package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionCreateEvent
import com.atlassian.sal.api.net.ResponseException
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.ProductsKeyMap
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Field


@Field pageId
@Field getPageInfo
@Field spaceInfo
@Field spaceHomePage
@Field wiki = 'Wiki'

def mainMethod() {
  
    //Run script with admin user
     CommonUtil.executeScriptWithAdmin()
    def event = event as VersionCreateEvent
    def pageTitle = event.version.name
    //def pageTitle = "Release1.0.3.5" // for Testing Purpose
    def pKey = event.version.project.key
    def mappedPKey = ProductsKeyMap.getWikiKey(pKey).toString()
    log.warn(mappedPKey)
    //def pageTitle = "JD" // for Testing Purpose
    try {
        getPageInfo =DoRequestCall.getRestCall(wiki , "rest/api/content/search?cql=space='${mappedPKey}'%20and%20title=Releases")
        pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
        spaceInfo = DoRequestCall.getRestCall(wiki , "rest/api/space/'${mappedPKey}'?expand=homepage")
        spaceHomePage = new JsonSlurper().parseText(spaceInfo.toString())?.homepage?.id
        log.warn("Page Id is " + pageId)
        log.warn("spaceHomePage Id is " + spaceHomePage)

    }catch(Exception e){
        log.warn(e)
    }

    def createReleasesDirectoryParams = [
        type     : "page",
        title    : "Releases",
        space    : [
            key: mappedPKey // set the space key - or calculate it from the project or something
        ],
        ancestors: [
            [
                type: "page",
                id  : spaceHomePage,
            ]
        ],
        body     : [
            storage: [
                value         : "",
                representation: "storage"
            ]
        ]
    ]

    def createReleaseNotePageParams = [
        type     : "page",
        title    : pageTitle,
        space    : [
            key: mappedPKey // set the space key - or calculate it from the project or something
        ],
        ancestors: [
            [
                type: "page",
                id  : pageId,
            ]
        ],
        body     : [
            storage: [
                value      : """<ac:layout>
                                    <ac:layout-section ac:type="single">
                                        <ac:layout-cell>
                                            <p>
                                                <ac:placeholder>Please Enter Project Name and  Release number  </ac:placeholder>
                                            </p>
                                        </ac:layout-cell>
                                    </ac:layout-section>
                                    <ac:layout-section ac:type="two_right_sidebar">
                                        <ac:layout-cell>
                                            <h1>
                                                <strong>Backup Procedure</strong>
                                            </h1>
                                            <table class="relative-table wrapped" style="width: 89.1892%;">
                                                <colgroup>
                                                    <col/>
                                                    <col style="width: 79.8394%;"/>
                                                    <col style="width: 16.7707%;"/>
                                                </colgroup>
                                                <thead>
                                                <tr>
                                                    <td class="numberingColumn">1</td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Action</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Team</strong>
                                                        </p>
                                                    </td>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td class="numberingColumn">2</td>
                                                    <td>
                                                        <p>
                                                            <br/>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <br/>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="numberingColumn">3</td>
                                                    <td>
                                                        <p>
                                                            <br/>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <br/>
                                                        </p>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                            <h1>
                                                <strong>Installation Procedure </strong>
                                            </h1>
                                            <table class="relative-table wrapped" style="width: 86.8045%;">
                                                <colgroup>
                                                    <col style="width: 2.65811%;"/>
                                                    <col style="width: 39.78%;"/>
                                                    <col style="width: 38.2218%;"/>
                                                    <col style="width: 15.857%;"/>
                                                </colgroup>
                                                <thead>
                                                <tr>
                                                    <td class="numberingColumn">1</td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Action</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Team</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Comment</strong>
                                                        </p>
                                                    </td>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td class="numberingColumn">2</td>
                                                    <td>
                                                        <p>
                                                            <br/>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                </tr>
                                                <tr>
                                                    <td class="numberingColumn">3</td>
                                                    <td>
                                                        <p>
                                                            <br/>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                            <h1>
                                                <strong>Rollback Procedure</strong>
                                            </h1>
                                            <table class="relative-table wrapped" style="width: 84.2607%;">
                                                <colgroup>
                                                    <col style="width: 2.73843%;"/>
                                                    <col style="width: 32.8612%;"/>
                                                    <col style="width: 50.1416%;"/>
                                                    <col style="width: 10.6704%;"/>
                                                </colgroup>
                                                <thead>
                                                <tr>
                                                    <td class="numberingColumn">1</td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Action</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Source code file</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Team</strong>
                                                        </p>
                                                    </td>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td class="numberingColumn">2</td>
                                                    <td>
                                                        <p>
                                                            <br/>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                            <p>
                                                <br/>
                                            </p>
                                            <h1>
                                                <strong>Release</strong>
                                            </h1>
                                            <p>
                                                <strong>
                                                    <br/>
                                                </strong>
                                            </p>
                                            <table class="relative-table wrapped" style="width: 84.0223%;">
                                                <colgroup>
                                                    <col/>
                                                    <col style="width: 34.0909%;"/>
                                                    <col style="width: 52.178%;"/>
                                                    <col style="width: 10.9848%;"/>
                                                </colgroup>
                                                <thead>
                                                <tr>
                                                    <td class="numberingColumn">1</td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Release Number</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>URL</strong>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p align="center">
                                                            <strong>Team</strong>
                                                        </p>
                                                    </td>
                                                </tr>
                                                </thead>
                                                <tbody>
                                                <tr>
                                                    <td class="numberingColumn">2</td>
                                                    <td>
                                                        <p>
                                                            <a href="https://jira.elm.sa/projects/${pKey}/versions/${event.version.id}">${event.version.name}</a>
                                                        </p>
                                                    </td>
                                                    <td>
                                                        <p>
                                                            <a href="">BuildURL</a>
                                                        </p>                                                    
                                                        </td>
                                                    <td>
                                                        <br/>
                                                    </td>
                                                </tr>
                                                </tbody>
                                            </table>
                                            <p>
                                                <strong>
                                                    <br/>
                                                </strong>
                                            </p>
                                            <h1>
                                                <strong>Delivered Features</strong>
                                            </h1>
                                            <p>
                                                <strong> <ac:structured-macro ac:macro-id="49827374-89e5-450d-b09a-0df11a4c1ca0" ac:name="jira" ac:schema-version="1">
                                                    <ac:parameter ac:name="server">JIRA</ac:parameter>
                                                    <ac:parameter ac:name="columns">key,summary,type,created,updated,due,assignee,reporter,priority,status,resolution</ac:parameter>
                                                    <ac:parameter ac:name="maximumIssues">20</ac:parameter>
                                                    <ac:parameter ac:name="jqlQuery">project =${pKey} and fixversion= ${event.version.id} and status in ('Awaiting Release',Done)</ac:parameter>
                                                    <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                                </ac:structured-macro> </strong>
                                            </p>
                                            <p>
                                                <strong>
                                                    <br/>
                                                </strong>
                                            </p>
                                        </ac:layout-cell>
                                        <ac:layout-cell>
                                            <p>
                                                <br/>
                                            </p>
                                            <p>
                                                <ac:structured-macro ac:macro-id="6cf6801a-ca29-4a52-a885-c2a69cc51d40" ac:name="toc" ac:schema-version="1"/>
                                            </p>
                                            <p>
                                                <br/>
                                            </p>
                                        </ac:layout-cell>
                                    </ac:layout-section>
                                    <ac:layout-section ac:type="single">
                                        <ac:layout-cell>
                                            <p>
                                                <br/>
                                            </p>
                                        </ac:layout-cell>
                                    </ac:layout-section>
                                </ac:layout>""",
                representation: "storage"
            ]
        ]
    ]

    def createReleasesDirectoryParamsJSON = new JsonBuilder(createReleasesDirectoryParams).toString()
    def createReleaseNotePageParamsJSON = new JsonBuilder(createReleaseNotePageParams).toString()

    if (!pageId) {
        try {
            DoRequestCall.postRestCall(createReleasesDirectoryParamsJSON, wiki, "rest/api/content")
            DoRequestCall.postRestCall(createReleaseNotePageParamsJSON, wiki, "rest/api/content")
        } catch (ResponseException e) {
           log.warn(e)
        }
    } else {
        try {
            DoRequestCall.postRestCall(createReleaseNotePageParamsJSON, wiki, "rest/api/content")
        } catch (ResponseException e) {
            log.warn(e)
        }
    }
}

mainMethod()
