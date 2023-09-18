package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.AfterQACertificate

import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.elm.scriptrunner.scriptfields.WokringMinutesCalculator
import com.google.gson.Gson
import groovy.json.JsonSlurper
import groovy.transform.Field

import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

/**
 * a post function that updates the change description with the release note page
 */

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")
def fixVersion = URLEncoder.encode(issue.fixVersions.first().name.replaceAll("\\s",""), "UTF-8")
def wikiKey = ProductsKeyMap.getWikiKey(issue.projectObject.key).toString()
def releaseNoteURL = "https://wiki.elm.sa/display/${wikiKey}/${fixVersion}".toString()
def issueDescWithReleaseNote = issue.description.concat(" \n\r Release Note Page: "+ releaseNoteURL)
ComponentAccessor.issueManager.updateIssue(Globals.botUser, issue, EventDispatchOption.ISSUE_UPDATED, false)


