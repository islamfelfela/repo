package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.json.JsonBuilder
import groovy.transform.Field

//@Field def targetedRepo
@Field def createBitbucketRepoParams


def createGitRepo() {
    //Run script with admin user
    def powerUser = CommonUtil.executeScriptWithAdmin()
    //  def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("DCP-41")// for testing purposes only
    def requestType = CommonUtil.getCustomFieldValue(issue, 11202).toString()
    def cFtargetRepoName = kabebCase(CommonUtil.getCustomFieldValue(issue, 11802).toString()).toLowerCase()
    if (requestType == "sup/0f1cb2d3-e4f5-424c-bf00-5e94f8a46ea0") {

        def projectManager = ComponentAccessor.getProjectManager()

        //String userPKey = CommonUtil.getCustomFieldValue(issue, 11503)
        def userPKey = CommonUtil.getInsightCField(issue,14805,'JiraKey').first() //442 is JIRA attribute insight Obj
        def projectObject = projectManager.getProjectObj(userPKey as long)

        def jiraPKey = projectObject.key
//            targetedRepo = cFtargetRepoName
        createBitbucketRepoParams = [
            name    : cFtargetRepoName,
            scmId   : "git",
            forkable: true
        ]

        log.warn(jiraPKey)
        def createBitbucketRepoParamsJSON = new JsonBuilder(createBitbucketRepoParams).toString()
        def createRepoRestUrl = "/rest/api/1.0/projects/${jiraPKey}/repos"
        try {
            DoRequestCall.postRestCall(createBitbucketRepoParamsJSON, Globals.bitbucket, createRepoRestUrl)
            def comment = "Repo has been created , you can access with https://bitbucket.elm.sa/projects/${jiraPKey}/repos/${cFtargetRepoName}/browse"
            CommonUtil.addCommentToIssue(powerUser, comment, issue)
        } catch (Exception e) {
            throw e
        }
    }
}


def kabebCase(str) {
  // more details : https://stackoverflow.com/a/17043843
  return str.toLowerCase().trim().replaceAll(/\ |_/, '-').replaceAll(/\B[A-Z]/) { '-' + it }
}

createGitRepo()