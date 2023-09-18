package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.project.ProjectCategory
import com.atlassian.jira.util.SimpleErrorCollection
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.onresolve.scriptrunner.canned.jira.admin.CopyProject
import com.onresolve.scriptrunner.runner.util.UserMessageUtil
import groovy.json.JsonBuilder
import groovy.transform.Field

//@Field def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("SUP-2601")//For Testing Purpose uncomment this line

@Field def destProjectKey
@Field def destProjectName
@Field def proectCategory

//sendJiraEmail = new SendJiraEmail()
def mainMethod() {
    def addComment = "Please verify the projects from below URL's if they are created successfully....\n\n\n"
    //Run script with admin user
    CommonUtil.executeScriptWithAdmin('atlassbot')
    def errorCollection = new SimpleErrorCollection()
    def issueComponenet = issue.components
    destProjectKey = CommonUtil.getCustomFieldValue(issue, 11502).toString().trim()
    log.warn("Targeted PKey: " + destProjectKey)
    destProjectName = CommonUtil.getCustomFieldValue(issue, 11503).toString()
    proectCategory = CommonUtil.getCustomFieldValue(issue, 11604).toString()
    log.warn("Targeted PName: " + destProjectName)
    def srcProjectKey = 'TEMPKAMP'
    def createRepoRestUrl = "/rest/api/1.0/projects/${destProjectKey}/repos"
    def createConfluenceProjectRestUrl = "rest/api/space"
    def createBitbucketProjectRestUrl = "rest/api/1.0/projects"

    def createBitbucketProjectParams = [
        key : destProjectKey,
        name: destProjectName
    ]
    def createConfluenceProjectParams = new JsonBuilder([
        key        : destProjectKey,
        name       : destProjectName,
        description: [
            plain: [
                value         : "Space created automatically for ${destProjectKey} project on " + new Date().format("dd/MMM/yyyy HH:mm"),
                representation: "plain"
            ]
        ]
    ]).toString()
    def createBitbucketRepoParams = new JsonBuilder([
        name    : destProjectKey,
        scmId   : "git",
        forkable: true
    ]).toString()
    def requestType = CommonUtil.getCustomFieldValue(issue, 11202).toString()
    // issue.getCustomFieldValue(customFieldManager.getCustomFieldObject(11202)).toString()
    if (requestType == Globals.RequestTypes.project_creation) { // Request Key for Project Creation
        issueComponenet.each {
            if (it.name == Globals.jira) {
                if (!ComponentAccessor.getProjectManager().getProjectByCurrentKey(destProjectKey.toString())) {
                    log.warn("Creating the JIRA Project\n" + "Project Key: ${destProjectKey};  Project Name: ${destProjectName}")
                    Thread executorThread = new Thread(new Runnable() {
                        void run() {
                            void
                            def copyProject = new CopyProject()
                            def inputs = [
                                (CopyProject.FIELD_SOURCE_PROJECT)       : srcProjectKey,
                                (CopyProject.FIELD_TARGET_PROJECT)       : destProjectKey,
                                (CopyProject.FIELD_TARGET_PROJECT_NAME)  : destProjectName,
                                (CopyProject.FIELD_CLONE_BOARD_NAME)     : "TEMPKAMP",
                                (CopyProject.FIELD_COPY_VERSIONS)        : true,
                                (CopyProject.FIELD_COPY_COMPONENTS)      : false,
                                (CopyProject.FIELD_COPY_ISSUES)          : false,
                                (CopyProject.FIELD_TARGET_BOARD_NAME)    : destProjectName + "-Board",
                                (CopyProject.FIELD_COPY_DASH_AND_FILTERS): true
                            ]
                            errorCollection = copyProject.doValidate(inputs, false)
                            if (errorCollection.hasAnyErrors()) {
                                log.warn("Couldn't create project: " + errorCollection)
                            } else {
                                copyProject.doScript(inputs)
                            }
                        }
                    })
                    executorThread.run()
                    def projectManager = ComponentAccessor.getProjectManager()
                    def project = projectManager.getProjectObjByKey(destProjectKey.toString())
                    ProjectCategory projectCategory = ComponentAccessor.getProjectManager().getProjectCategoryObjectByName(proectCategory.toString())
                    projectManager.setProjectCategory(project, projectCategory)
                    UserMessageUtil.success("Jira Project Created Successfully")
                } else {
                    UserMessageUtil.info("Can't create Project on Jira with the same Key")
                }

                addComment += "Component:Jira\nProject URL: https://jira.elm.sa/projects/${destProjectKey}\n-------------------\n"
            }
            else if (it.name == Globals.bitbucket) {
                try {
                    def createBitbucketProjectParamsJSON = new JsonBuilder(createBitbucketProjectParams).toString()
                    def createBitbucketRepoParamsJSON = new JsonBuilder(createBitbucketRepoParams).toString()

                    DoRequestCall.postRestCall(createBitbucketProjectParamsJSON, Globals.bitbucket, createBitbucketProjectRestUrl)
                    DoRequestCall.postRestCall(createBitbucketRepoParamsJSON, Globals.bitbucket, createRepoRestUrl)
                    addComment += "Component:Bitbucket\nProject URL: https://bitbucket.elm.sa/projects/${destProjectKey}\n-------------------\n"

                } catch (Exception e) {
                    addComment += "Component:Bitbucket\nProject URL: https://bitbucket.elm.sa/projects/${destProjectKey}\nAn error occured while creating Bitbucket Project/Repository\n$e\n-------------------\n"
                    log.warn('An error occurred while creating Bitbucket Project' + e)
                }
            }
            else if (it.name == Globals.wiki) {
                try {
                    DoRequestCall.postRestCall(createConfluenceProjectParams, Globals.wiki, createConfluenceProjectRestUrl)
                    addComment += "Component:WIKI\nProject URL: https://wiki.elm.sa/display/${destProjectKey}\n-------------------\n"
                } catch (Exception e) {
                    addComment += "Component:WIKI\nProject URL: https://wiki.elm.sa/display/${destProjectKey}\n An error occured while creating the Wiki Space\n$e\n-------------------\n"
                    log.warn('An error occurred while creating WIKI Project' + e)
                }
            }
            else if (it.name == Globals.bamboo) {
                def bambooReqParameters = new JsonBuilder([
                    'name': destProjectName,
                    'key' : destProjectKey
                ]).toString()
                def bambooPermissions = new JsonBuilder(["READ"]).toString()
                try {
                    DoRequestCall.postRestCall(bambooReqParameters, Globals.bamboo, 'rest/api/latest/project?showEmpty')
                    DoRequestCall.putRestCall(bambooPermissions, Globals.bamboo, "rest/api/latest/permissions/project/${destProjectKey.toString()}/users/internaudit")
                    DoRequestCall.putRestCall(bambooPermissions, Globals.bamboo, "rest/api/latest/permissions/project/${destProjectKey.toString()}/groups/DCS_Ops_Apps")
                    DoRequestCall.putRestCall(bambooPermissions, Globals.bamboo, "rest/api/latest/permissions/projectplan/${destProjectKey}/users/internaudit")
                    DoRequestCall.putRestCall(bambooPermissions, Globals.bamboo, "rest/api/latest/permissions/projectplan/${destProjectKey.toString()}/groups/DCS_Ops_Apps")
                    addComment += "Component:Bamboo\nProject URL:https://bamboo.elm.sa/browse/${destProjectKey}\n-------------------\n"
                } catch (Exception e) {
                    addComment += "Component:Bamboo\nProject URL:https://bamboo.elm.sa/browse/${destProjectKey}\nAn error occured while creating Bamboo Project\n$e\n-------------------\n"
                    log.warn('An error occured while creating Bamboo Project' + e)
                }
            }
            else if (it.name == Globals.jenkins) {
                if(isProjectExist(destProjectKey)){
                    def uriPathFolderKey = "createItem"
                    def uriPathFolderName = "job/${destProjectKey}/configSubmit"
                    def jenkinsFKey = ["name": "${destProjectKey}", "mode": "com.cloudbees.hudson.plugins.folder.Folder"]
                    def fName = '''{"displayNameOrNull": "destProjectName", "description": "", "": "0", 
                                    "icon": {"stapler-class": "com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon",
                                    '$class': "com.cloudbees.hudson.plugins.folder.icons.StockFolderIcon"},
                                    "org-csanchez-jenkins-plugins-kubernetes-KubernetesFolderProperty": {"": false}, "core:apply": ""}'''
                        .replace('destProjectName',destProjectName)
                    def jenkinsFName = ["json":fName ]
                    HttpRestUtil.JPost(uriPathFolderKey,jenkinsFKey)
                    HttpRestUtil.JPost(uriPathFolderName,jenkinsFName)
                    addComment += "Component:Jenkins\nProject URL:https://pipeline.devops.elm.sa/job/${destProjectKey}\n-------------------\n"

                }
            }
        }
        CommonUtil.addCommentToIssue(Globals.botUser,addComment,issue)
    }
}

static boolean  isProjectExist(def destProjectKey){
    def uriPathCurrentFolder = "job/${destProjectKey}/"
    return HttpRestUtil.JGet(uriPathCurrentFolder).status == 200
}

mainMethod()