package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.util.json.JSONObject
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

@Field issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SUP-6734")



    CommentManager commentManager = ComponentAccessor.getCommentManager()
    def properties = [(Globals.SD_PUBLIC_COMMENT): new JSONObject(["internal": true])]
    commentManager.create(issue, issue.reporterUser, 'comnts', null, null, new Date(), properties, true)

