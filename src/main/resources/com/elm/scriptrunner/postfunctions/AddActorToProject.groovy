package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.bc.projectroles.ProjectRoleService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.security.roles.ProjectRoleActor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.SimpleErrorCollection
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.sal.api.net.ResponseException
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.google.gson.Gson
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.Field

//@Field issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SUP-3359")
@Field requestType
@Field List<String> excludedProjectCategory = ["Archived", "Support"]
def commentsCopy=""
def projectManager = ComponentAccessor.getProjectManager()
def projectRoleService = ComponentAccessor.getComponent(ProjectRoleService)
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
def errorCollection = new SimpleErrorCollection()
//def projectsList =   CommonUtil.getCustomFieldValue(issue,11503).toString().split(',')
def projectsList = []
try {
    projectsList = CommonUtil.getInsightCField(issue, 14805, "JiraKey")
}catch(Exception e ) {
    commentsCopy += "Project can't be find  + ${e.message}}"
    addCommentToIssue(Globals.botUser, commentsCopy, issue)
}

def userRole = CommonUtil.getCustomFieldValue(issue,11303).toString()
def reportUser
ApplicationUser requestUser
def issueComponenet = issue.components
def projectRole = projectRoleManager.getProjectRole(userRole)

requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()

if (requestType in [Globals.RequestTypes.applicationAccess, Globals.RequestTypes.applicationAccessTrainee]) {
    reportUser = issue.reporter
} else if (requestType == Globals.RequestTypes.project_creation){
log.warn('Create New Project PostFunction will be triggered')
}else {
    requestUser = (ApplicationUser) CommonUtil.getCustomFieldValue(issue,12202)
    if (requestUser) {
        reportUser = requestUser
        log.warn(requestUser)
    }else {
        return
    }
}
addUsersToCrowd(issueComponenet, [reportUser.username.toLowerCase()])

projectsList.each { project ->
    CommonUtil.executeScriptWithAdmin('atlassbot')

    def projectObject = projectManager.getProjectObj(project as long)

    def projectCategory = projectManager.getProjectCategoryForProject(projectObject)
    log.warn(projectCategory)
    if (!excludedProjectCategory.contains(projectCategory.name.toString())) {
        def bambooBPException = "An error occured on bamboo build plans for project: $projectObject "
        def bambooDPException = "An error occured on bamboo deployment plans for project: $projectObject "
        def bambooErrorMsgDplan = "Either deployement plan doest not exist or the project key in Bamboo is different for project: $projectObject"
        issueComponenet.each {
//            def pKey = projectManager.getProjectObjByName(project.toString())?.getKey()
            def pKey = projectObject?.key

            //def jiraProject = projectManager.getProjectObjByKey(pKey)

            def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
            if (it.name == Globals.wiki) {
                commentsCopy+=addUsersToWiki([reportUser.username.toLowerCase()], wikiKey, projectObject)

            } else if (it.name == Globals.jira) {
                commentsCopy+=addUsersToJira(projectRoleService, [reportUser.key], projectRole, projectObject, errorCollection, projectObject, projectRoleManager, requestUser)
            } else if (it.name == Globals.bitbucket) {
                commentsCopy+=addUsersToBitbucket(projectRole, [reportUser.username.toLowerCase()], projectObject, pKey)

            } else if (it.name == Globals.bamboo) {
                commentsCopy+=addUsersToBamboo(pKey, [reportUser.username.toLowerCase()], projectRole, projectObject, bambooErrorMsgDplan, bambooDPException, bambooBPException)
            }
        }
    } else {
        commentsCopy += "Cannot add to project : ${projectObject} , because system Doesn't Allow adding Role to  project under category:  ${projectCategory.name} \n--------\n"
    }
}

addCommentToIssue(Globals.botUser, commentsCopy,issue)

def addCommentToIssue(def LogedInUser, def comnts,def issue) {
    CommentManager commentManager = ComponentAccessor.getCommentManager()
    def properties = [(Globals.SD_PUBLIC_COMMENT): new JSONObject(["internal": true])]
    commentManager.create(issue, LogedInUser, comnts, null, null, new Date(), properties, true)
}

def addUsersToCrowd(def component, def reportUser) {
    component.each {
        String crowdUser = new Gson().toJson(["name": reportUser.get(0)])
        if (it.name == Globals.wiki) {
            HttpRestUtil.crowdRestCall([groupname: 'confluence users'],crowdUser)
        } else if (it.name == Globals.bitbucket) {
            HttpRestUtil.crowdRestCall([groupname: 'bitbucket-users'],crowdUser)
        } else if (it.name == Globals.bamboo) {
            HttpRestUtil.crowdRestCall([groupname: 'bamboo-users'],crowdUser)
        } else if (it.name == Globals.jira) {
            HttpRestUtil.crowdRestCall([groupname: 'jira users'],crowdUser)
        }
    }
}

def addUsersToBitbucket(def projectRole, def reportUser, def project, def pKey) {
    def addComment=""

    def bitbucketKey = ProductsKeyMap.getBitbucketKey(pKey).toString()
    def bitbucketRestCallDev = "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser.get(0)}&permission=PROJECT_WRITE"
    def bitbucketRestCallAll = "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser.get(0)}&permission=PROJECT_READ"
    if (projectRole.name in ['Developers','Contributor']) {
        try {
            DoRequestCall.putRestCall('',Globals.bitbucket,bitbucketRestCallDev)
        }
        catch (ResponseException e) {
            //throw e
        }
        catch (Exception e) {
            // throw e
        }
    } else {
        try {
            DoRequestCall.putRestCall('', Globals.bitbucket, bitbucketRestCallAll)
        }
        catch (ResponseException e) {
            //throw e
        }
        catch (Exception e) {
            //  throw e
        }
    }
    //get users permissions for bitbucket
    try {
        def getbitBucketUsers = DoRequestCall.getRestCall(Globals.bitbucket,
            "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser.get(0)}&limit=300")
        def getbitBucketUsersP = new JsonSlurper().parseText(getbitBucketUsers.toString())
        getbitBucketUsersP.values.each {
            if (it.user.name == reportUser.get(0))
                addComment += "Component:Bitbucket\nProject Name:${project}\nUser Name: ${it.user.displayName}\nPermissions: ${it.permission}\n-------\n"
            return true
        }
    } catch (ResponseException e) {
        addComment += "Either project: $project does not exist on Bitbucket or project key is different\n$e\n--------\n"
    }
    catch (Exception e) {
        addComment += "Exception occured on Bitbucket for project: $project\n$e\n--------\n"
    }
    //get users permissions for bitbucket end
    return addComment

}

def addUsersToWiki(def reportUser, def wikiKey, def project) {
    def addComment=""
    def wikiPermissions = "?VIEWSPACE=VIEWSPACE&EDITSPACE=EDITSPACE&COMMENT=COMMENT&CREATEATTACHMENT=CREATEATTACHMENT&REMOVEOWNCONTENT=REMOVEOWNCONTENT&user=${reportUser.get(0)}&space=${wikiKey}"
    def wikiBaseURL = "https://wiki.elm.sa/rest/keplerrominfo/refapp/latest/webhooks/WIKIPermission/run${wikiPermissions}"
    try {
        DoRequestCall.wikiPermissionRestCall(wikiBaseURL)
    }
    catch (Exception e) {
    }
    def wikiUsers = DoRequestCall.wikiPermissionRestCall(wikiBaseURL)
    addComment += "Component:WIKI$wikiKey\nProject Name:${project}\nUser Name: ${reportUser.get(0)}\nPermissions: ${wikiUsers}\n------------\n"
    return addComment
}

def addUsersToJira(def projectRoleService, def reportUser, def projectRole, def jiraProject, def errorCollection, def project, def projectRoleManager, def requestUser) {
    def addComment=""

    try {

        projectRoleService.addActorsToProjectRole(reportUser , projectRole, jiraProject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE, errorCollection)
    }
    catch (Exception e) {
        addComment += "Component:JIRA\nProject Name:${project}\nUser Name: ${reportUser.get(0)}\n$e\n-------------\n"
    }
    try {
        def userPermissions
        if (requestType == Globals.RequestTypes.applicationAccess) {
            userPermissions = projectRoleManager.getProjectRoles(issue.reporter, jiraProject)
        } else {
            userPermissions = projectRoleManager.getProjectRoles(requestUser, jiraProject)
        }
        addComment += "Component:JIRA\nProject Name:${project}\nUser Name: ${reportUser.get(0)}\nPermissions: ${userPermissions}\n-------------------\n"
    } catch (Exception e) {
        addComment += "Component:JIRA\nProject Name:${project}\nUser Name: ${reportUser.get(0)}\nPermissions: ${userPermissions}\n-------------------\n"

    }

    return addComment
}

def addUsersToBamboo(def pKey, def reportUser, def projectRole, def project, def bambooErrorMsgDplan, def bambooDPException, def bambooBPException) {
    def addComment=""

    def qaPermissionDeplPlan = new JsonBuilder(["READ"]).toString()
    def devPermissionDeplPlan= new JsonBuilder(["READ","VIEWCONFIGURATION", "WRITE"]).toString()
    def qaPermission =new JsonBuilder(["READ"]).toString()
    def devPermission = new JsonBuilder(["READ","VIEWCONFIGURATION", "BUILD", "WRITE"]).toString()
    def bambooPKey = ProductsKeyMap.getBambooKey(pKey).toString()
    try {
        DoRequestCall.putRestCall(qaPermissionDeplPlan, Globals.bamboo,
            "rest/api/latest/permissions/project/${bambooPKey}/users/${reportUser.get(0)}?limit=500")
    } catch (ResponseException e) {
    }
    def bambooRestCall = "rest/api/latest/permissions/projectplan/${bambooPKey}/users/${reportUser.get(0)}?limit=500"
    try {
        if (projectRole.name in ['Developers','Contributor']) {
            try {
                DoRequestCall.putRestCall(devPermission, Globals.bamboo, bambooRestCall)
            } catch (Exception e) {
                // throw e
            }
            bambooDeploymentPlans(bambooPKey, devPermissionDeplPlan, reportUser.get(0))

        } else {
            try {
                DoRequestCall.putRestCall(qaPermission, Globals.bamboo, bambooRestCall)
            } catch (Exception e) {
                // throw e
            }

            bambooDeploymentPlans(bambooPKey, devPermissionDeplPlan, reportUser.get(0))
        }
    } catch (Exception e) {
        //throw e
    }
    //get users permissions for bamboo build plans
    try {
        def getBambooUsers = DoRequestCall.getRestCall(Globals.bamboo,
            "rest/api/latest/permissions/projectplan/${pKey}/users?limit=500")
        def getBambooUsersP = new JsonSlurper().parseText(getBambooUsers.toString())
        getBambooUsersP.results.each {
            if (it.name == reportUser.get(0))
                addComment += "Component:Bamboo Build Plan\nProject Name:${project}\nUser Name: ${it.fullName}\nPermissions: ${it.permissions}\n-------------------\n"
            // log.debug(addComment)
            return true
        }
    } catch (ResponseException e) {
        addComment += "Component:Bamboo Build Plan\n" + "No build plan exist for this project" + "\n" + "\n----------\n"
    }
    catch (Exception e) {
        addComment += "Component:Bamboo Build Plan\n" + bambooBPException + "\n" + e + "\n----------\n"
    }
    //get users permissions for bamboo deployment plans
    try {
        def planKeys = DoRequestCall.getRestCall(Globals.bamboo,
            "rest/api/1.0/project/${bambooPKey}.json?expand=plans&max-result=500")

        def parsedJson = new JsonSlurper().parseText(planKeys.toString())
        parsedJson.plans.plan.each { plansKeys ->
            // log.debug("plan: $plansKeys.key")
            String deploymentPlans = DoRequestCall.getRestCall(Globals.bamboo,
                "/rest/api/latest/deploy/project/forPlan?planKey=${plansKeys.key}")
            if (deploymentPlans != "[]") {
                def deploymentPlanP = new JsonSlurper().parseText(deploymentPlans)
                String deploymentPlanId = deploymentPlanP.id
                String deploymentPlanName = deploymentPlanP.name
                def deploymentPlanIdc = deploymentPlanId.substring(1, deploymentPlanId.length() - 1)
                def getBambooDepPUsers = DoRequestCall.getRestCall(Globals.bamboo,
                    "rest/api/latest/permissions/deployment/${deploymentPlanIdc}/users?limit=500")
                def getBambooDepPUsersP = new JsonSlurper().parseText(getBambooDepPUsers.toString())
                getBambooDepPUsersP.results.each {
                    if (it.name == reportUser.get(0))
                        addComment += "Component:Bamboo Deployment Plan\nProject Name:${project}\nDeployment Plan: ${deploymentPlanName}\nUser Name: ${it.fullName}\nPermissions: ${it.permissions}\n-------------------\n"
                    return true
                }
            }
        }
    } catch (ResponseException e) {
        addComment += "Component:Bamboo Deployment Plan\n" + "No deployment plan exist for this project" + "\n" + "\n-------------------\n"
    }
    catch (Exception e) {
        addComment += "Component:Bamboo Build Plan\n" + bambooDPException + "\n" + e + "\n-------------------\n"
    }
    return addComment
}

def bambooDeploymentPlans(def pKey, String permissions, def reportUser) {
    try {
        def planKeys = DoRequestCall.getRestCall(Globals.bamboo, "rest/api/1.0/project/${pKey}.json?expand=plans&max-result=500")
        def parsedJson = new JsonSlurper().parseText(planKeys)

        parsedJson.plans.plan.each { plansKeys ->
            String deploymentPlans = DoRequestCall.getRestCall(Globals.bamboo,
                "/rest/api/latest/deploy/project/forPlan?planKey=${plansKeys.key}")
            if (deploymentPlans != "[]") {
                def deploymentPlanP = new JsonSlurper().parseText(deploymentPlans)
                String deploymentPlanId = deploymentPlanP.id
                log.warn("rest/api/latest/permissions/deployment/${deploymentPlanId.toString()}/users/${reportUser}?limit=100")
                def deploymentPlanIdc = deploymentPlanId.substring(1, deploymentPlanId.length() - 1)
                try {
                    DoRequestCall.putRestCall(permissions, Globals.bamboo,
                        "rest/api/latest/permissions/deployment/${deploymentPlanIdc.toString()}/users/${reportUser}?limit=100")
                } catch (ResponseException e) {
                    // throw e
                }
            }
        }
    }
    catch (ResponseException e) {
        // throw e
    }
    catch (Exception e) {
        //throw e
    }
}
