package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
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
    CommonUtil.executeScriptWithAdmin("atlassbot")
    def event = event as IssueEvent
    def issueType = event.issue.issueType.name
    def issueStatus = event.issue.status.name

    if (event.eventTypeId == 13 && issueStatus == "Scheduling" && issueType == "Change" ) {

        def pKey = event.issue.projectObject.key

            def fixVersion = event.issue.fixVersions.first()
            def mappedPKey = ProductsKeyMap.getWikiKey(pKey).toString()
            log.warn(mappedPKey)
            //def pageTitle = "JD" // for Testing Purpose
            try {
                getPageInfo = DoRequestCall.getRestCall(wiki, "rest/api/content/search?cql=space='${mappedPKey}'%20and%20title=QA_Reports")
                
                pageId = new JsonSlurper().parseText(getPageInfo.toString()).results.id.get(0)
               
                spaceInfo = DoRequestCall.getRestCall(wiki, "rest/api/space/'${mappedPKey}'?expand=homepage")
                spaceHomePage = new JsonSlurper().parseText(spaceInfo.toString())?.homepage?.id
                log.warn("Page Id is " + pageId)
                log.warn("spaceHomePage Id is " + spaceHomePage)

            } catch (Exception e) {
                log.warn(e)
            }

            def createReleasesDirectoryParams = [
                type     : "page",
                title    : "QA_Reports",
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
                title    : fixVersion,
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
                        value         : """<h1>
                                      <strong>
                                        <br/>
                                      </strong>
                                    </h1>
                                    <table class="wrapped">
                                      <colgroup>
                                        <col/>
                                        <col/>
                                        <col/>
                                        <col/>
                                        <col/>
                                      </colgroup>
                                      <tbody>
                                        <tr>
                                          <th colspan="1">Application Type </th>
                                          <th colspan="1">Number Of Defects</th>
                                          <th>Number of Builds</th>
                                          <th>Current Cycle</th>
                                          <th>Total number of Cycles</th>
                                        </tr>
                                        <tr>
                                          <td colspan="1" style="text-align: center;">WEB</td>
                                          <td colspan="1">
                                            <div class="content-wrapper">
                                              <p>
                                                <ac:structured-macro ac:macro-id="0dae700c-e7ba-478a-a715-89af7cfb8b45" ac:name="jira" ac:schema-version="1">
                                                  <ac:parameter ac:name="server">Jira</ac:parameter>
                                                  <ac:parameter ac:name="jqlQuery">project = ${pKey} and issuetype in (Bug) and fixVersion = ${fixVersion} </ac:parameter>
                                                  <ac:parameter ac:name="count">true</ac:parameter>
                                                  <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                                </ac:structured-macro>
                                              </p>
                                            </div>
                                          </td>
                                          <td>
                                            <br/>
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
                                      <strong>List of Changes in This Release</strong>
                                    </h1>
                                    <p>
                                      <br/>
                                    </p>
                                    <p>
                                      <ac:structured-macro ac:macro-id="5c8dcbde-4784-4700-a24a-549343cf2113" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,issuetype,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,type,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">project = ${pKey} and issuetype in (Story) and fixVersion = ${fixVersion} </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Defects Fixed</strong>
                                    </h1>
                                    <p>
                                      <br/>
                                    </p>
                                    <p>
                                      <ac:structured-macro ac:macro-id="32e34a3f-8f65-44bf-a6e0-27e4ec63dc86" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,issuetype,assignee,reporter,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,type,assignee,reporter,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">project = ${pKey}and issuetype  in (Bug) and fixVersion = ${fixVersion} and status ="Awaiting Release" </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Open Defects</strong>
                                    </h1>
                                    <p>
                                      <ac:structured-macro ac:macro-id="58f86fd9-da82-406d-ae27-3a5cbf9eaba1" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,issuetype,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,type,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">project = ${pKey} and issuetype  in (Bug) and fixVersion = ${fixVersion} and status in (Open,Development,Deployment,Testing) </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Reopened Defects</strong>
                                    </h1>
                                    <p>
                                      <ac:structured-macro ac:macro-id="50e3153d-9f95-4b54-89d0-d7c33a346ac6" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,issuetype,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,type,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">issuetype = bug AND createdDate &gt;= startOfYear() AND (status changed from Testing to Development  OR status changed from Done to Development ) AND reporter in (aalzandi, aalomary, moalsharif, sgulay) AND project = "SEEC GEEP" and fixversion=1.0.6 </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Regression Defects</strong>
                                    </h1>
                                    <p>
                                      <ac:structured-macro ac:macro-id="e2ebef6c-b624-454d-b947-0c9d35497613" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,issuetype,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,type,assignee,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">project = ${pKey} and issuetype  in (Bug) and fixVersion = ${fixVersion} and "Regression Bug?" = Yes  </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Items Not Tested (out of scope)</strong>
                                    </h1>
                                    <p>
                                      <ac:structured-macro ac:macro-id="0efb188b-3675-4283-b58d-869e0f7115af" ac:name="jira" ac:schema-version="1">
                                        <ac:parameter ac:name="server">Jira</ac:parameter>
                                        <ac:parameter ac:name="columnIds">summary,assignee,reporter,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="columns">summary,assignee,reporter,priority,status,resolution</ac:parameter>
                                        <ac:parameter ac:name="maximumIssues">12</ac:parameter>
                                        <ac:parameter ac:name="jqlQuery">project = ${pKey} and issuetype  in (Bug,Story,Technical-Task) and fixVersion = ${fixVersion} and resolution ="Out Of Scope" </ac:parameter>
                                        <ac:parameter ac:name="serverId">078f5cb7-a93b-3298-82de-d699ab4c4d1d</ac:parameter>
                                      </ac:structured-macro>
                                    </p>
                                    <h1>
                                      <strong>Risks</strong>
                                    </h1>
                                    <p>
                                      <strong>
                                        <br/>
                                      </strong>
                                    </p>
                                    <table class="relative-table wrapped" style="width: 37.6851%;">
                                      <colgroup>
                                        <col style="width: 20.5539%;"/>
                                        <col style="width: 79.4461%;"/>
                                      </colgroup>
                                      <thead>
                                        <tr>
                                          <td style="text-align: left;">
                                            <p align="center">
                                              <strong>Release Number</strong>
                                            </p>
                                          </td>
                                          <td style="text-align: left;">
                                            <p align="center">
                                              <strong>Risks item</strong>
                                            </p>
                                          </td>
                                        </tr>
                                      </thead>
                                      <tbody>
                                        <tr>
                                          <td style="text-align: left;">
                                            <p>
                                              <a class="external-link" href="https://jira.elm.sa/projects/SEP/versions/26079" rel="nofollow">1.0.</a>6</p>
                                          </td>
                                          <td style="text-align: left;">
                                            <p>
                                              <br/>
                                            </p>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    <h1>
                                      <strong>Release</strong>
                                    </h1>
                                    <p>
                                      <strong>
                                        <br/>
                                      </strong>
                                    </p>
                                    <table class="relative-table wrapped" style="width: 36.2041%;">
                                      <colgroup>
                                        <col style="width: 5.76631%;"/>
                                        <col style="width: 23.217%;"/>
                                        <col style="width: 71.0167%;"/>
                                      </colgroup>
                                      <thead>
                                        <tr>
                                          <td style="text-align: left;">
                                            <br/>
                                          </td>
                                          <td style="text-align: left;">
                                            <p align="center">
                                              <strong>Release Number</strong>
                                            </p>
                                          </td>
                                          <td style="text-align: left;">
                                            <p align="center">
                                              <strong>URL</strong>
                                            </p>
                                          </td>
                                        </tr>
                                      </thead>
                                      <tbody>
                                        <tr>
                                          <td style="text-align: left;">1</td>
                                          <td style="text-align: left;">
                                            <p>
                                              <a class="external-link" href="https://jira.elm.sa/projects/SEP/versions/26079" rel="nofollow">1.0.</a>6</p>
                                          </td>
                                          <td style="text-align: left;">
                                            <p>
                                              <br/>
                                            </p>
                                          </td>
                                        </tr>
                                      </tbody>
                                    </table>
                                    """,
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
                    log.warn(e.message)
                }
            } else {
                try {
                    DoRequestCall.postRestCall(createReleaseNotePageParamsJSON, wiki, "rest/api/content")
                } catch (ResponseException e) {
                    log.warn(e)
                }
            }
        }
    }



mainMethod()
