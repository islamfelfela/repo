package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.bc.projectroles.ProjectRoleService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleActor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.SimpleErrorCollection
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

@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("SUP-3359")
@Field String requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()
@Field List<String> excludedProjectCategory = ["Archived", "Support"]

@Field String addComment=""

if (requestType in [Globals.RequestTypes.applicationAccess, Globals.RequestTypes.applicationAccessTrainee]) {
    def reportUser = [issue.reporter?.name?.toLowerCase()]
} else if (requestType == Globals.RequestTypes.project_creation){
    log.warn('Create New Project PostFunction will be triggered')
}else {
    def requestUser = (ApplicationUser) CommonUtil.getCustomFieldValue(issue,12202)
    reportUser = [requestUser.getUsername().toLowerCase()]
    log.warn(requestUser)
}

def addActorToProject(def reportUser) {
    def projectManager = ComponentAccessor.getProjectManager()
    def projectsList = CommonUtil.getCustomFieldValue(issue, 11503).toString().split(',')
    def userRole = CommonUtil.getCustomFieldValue(issue,11303).toString()
    def projectRole = ComponentAccessor.getComponent(ProjectRoleManager).getProjectRole(userRole)
    addUsersToCrowd(issue.components, reportUser)

    projectsList.each { project ->
        def filterProjectObj = projectManager.getProjectObjByName(project).projectCategory.name in excludedProjectCategory
        if (!filterProjectObj) {
            issue.components.each {
                def pKey = projectManager.getProjectObjByName(project.toString())?.getKey()
                def jiraProject = projectManager.getProjectObjByKey(pKey)
                def wikiKey = ProductsKeyMap.getWikiKey(pKey).toString()
                if (it.name == Globals.wiki) {
                    commentsCopy += addUsersToWiki(reportUser, wikiKey, project)
                } else if (it.name == Globals.jira) {
                    commentsCopy += addUsersToJira(reportUser, projectRole, jiraProject)
                } else if (it.name == Globals.bitbucket) {
                    commentsCopy += addUsersToBitbucket(projectRole, reportUser, pKey)

                } else if (it.name == Globals.bamboo) {
                    commentsCopy += addUsersToBamboo(pKey, reportUser, projectRole)
                }
            }
        } else {
            commentsCopy += "Cannot add to project : ${project} , because system Doesn't Allow adding Role to  project under the selected category \n--------\n"
        }
    }
}

def addUsersToJira(def reportUser, def projectRole, def jiraProject) {
def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
    try {
        ComponentAccessor.getComponent(ProjectRoleService).
            addActorsToProjectRole(reportUser as List, projectRole, jiraProject, ProjectRoleActor.USER_ROLE_ACTOR_TYPE, new SimpleErrorCollection())
    }
    catch (Exception e) {
        addComment += "Component:JIRA\nProject Name:${jiraProject.name}\nUser Name: ${reportUser.get(0)}\n$e\n-------------\n"
    }
    try {
        def userPermissions
        if (requestType == Globals.RequestTypes.applicationAccess) {
            userPermissions = projectRoleManager.getProjectRoles(issue.reporter, jiraProject)
        }
//        else {
//            userPermissions = projectRoleManager.getProjectRoles(requestUser, jiraProject)
//        }
        addComment += "Component:JIRA\nProject Name:${jiraProject.key}\nUser Name: ${reportUser} \nPermissions: ${userPermissions}\n-------------------\n"
    } catch (Exception e) {
        addComment += "Component:JIRA\nProject Name:${jiraProject.key}\nUser Name: ${reportUser} \nPermissions: ${userPermissions}\n-------------------\n"
    }
    return addComment
}

def static addUsersToCrowd(def component, def reportUser) {
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

def addUsersToBitbucket(def projectRole, def reportUser, def pKey) {
    def bitbucketKey = ProductsKeyMap.getBitbucketKey(pKey).toString()
    def bitbucketRestCallDev = "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser}&permission=PROJECT_WRITE"
    def bitbucketRestCallAll = "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser}&permission=PROJECT_READ"
    if (projectRole.name == 'Contributor') {
        try {
            DoRequestCall.putRestCall('',Globals.bitbucket,bitbucketRestCallDev)

        } catch (Exception e) {
            //throw e
        }
    } else {
        try {
            DoRequestCall.putRestCall('', Globals.bitbucket, bitbucketRestCallAll)
        } catch (Exception e) {
            //  throw e
        }
    }
    //get users permissions for bitbucket end
    def getbitBucketUsers = getBitbucketPermission(reportUser,pKey)
}

def addUsersToWiki(def reportUser, def wikiKey, def project) {
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

def addUsersToBamboo(def pKey, def reportUser, def projectRole) {

    def contributeDeployPermission = new JsonBuilder(["READ", "WRITE"]).toString()
    def ContributePermission = new JsonBuilder(["READ", "BUILD", "WRITE"]).toString()
    def bambooPKey = ProductsKeyMap.getBambooKey(pKey).toString()
    try {
        DoRequestCall.putRestCall(contributeDeployPermission, Globals.bamboo,
            "rest/api/latest/permissions/project/${bambooPKey}/users/${reportUser.get(0)}?limit=500")
    } catch (ResponseException e) {
    }
    def bambooRestCall = "rest/api/latest/permissions/projectplan/${bambooPKey}/users/${reportUser.get(0)}?limit=500"
    try {
        if (projectRole.name == 'Contributor') {
            try {
                DoRequestCall.putRestCall(ContributePermission, Globals.bamboo, bambooRestCall)
            } catch (Exception e) {
                // throw e
            }
            bambooDeploymentPlans(bambooPKey, contributeDeployPermission, reportUser.get(0))

        }
    } catch (Exception e) {
        //throw e
    }
    return addComment
}

def bambooDeploymentPlans(def pKey, String permissions, def reportUser) {
    try {
        String planKeys = DoRequestCall.getRestCall(Globals.bamboo, "rest/api/1.0/project/${pKey}.json?expand=plans&max-result=500")
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
    catch (Exception e) {
        //throw e
    }
    getBambooBuildPermission(pKey, reportUser)
    getDeploymentPermission()
}

def getBambooBuildPermission(String pKey, String reportUser){
    //get users permissions for bamboo build plans
    try {
        def getBambooUsers = DoRequestCall.getRestCall(Globals.bamboo,
            "rest/api/latest/permissions/projectplan/${pKey}/users?limit=500")
        def getBambooUsersP = new JsonSlurper().parseText(getBambooUsers.toString())
        getBambooUsersP.results.each {
            if (it.name == reportUser)
                addComment += "Component:Bamboo Build Plan\nProject Name:${pKey}\nUser Name: ${it.fullName}\nPermissions: ${it.permissions}\n-------------------\n"
            // log.debug(addComment)
            return true
        }
    } catch (ResponseException e) {
        addComment += "Component:Bamboo Build Plan\n" + "No build plan exist for this project" + "\n" + "\n----------\n"
    }
    catch (Exception e) {
        addComment += "Component:Bamboo Build Plan\n" + "\n" + e + "\n----------\n"
    }
}

def getDeploymentPermission(String pKey, String reportUser){
    //get users permissions for bamboo deployment plans
    try {
        def planKeys = DoRequestCall.getRestCall(Globals.bamboo,
            "rest/api/1.0/project/${pKey}.json?expand=plans&max-result=500")
        def parsedJson = new JsonSlurper().parseText(planKeys.toString())
        parsedJson.plans.plan.each { plansKeys ->
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
                    if (it.name == reportUser)
                        addComment += "Component:Bamboo Deployment Plan\nProject Name:${pKey}\nDeployment Plan: ${deploymentPlanName}\nUser Name: ${it.fullName}\nPermissions: ${it.permissions}\n-------------------\n"
                    return true
                }
            }
        }
    } catch (ResponseException e) {
        addComment += "Component:Bamboo Deployment Plan\n" + "No deployment plan exist for this project" + "\n" + "\n-------------------\n"
    }
    catch (Exception e) {
        addComment += "Component:Bamboo Build Plan\n" + "\n" + e + "\n-------------------\n"
    }
}

def getBitbucketPermission(String reportUser, def bitbucketKey) {
    //get users permissions for bitbucket
    try {
        def getbitBucketUsers = DoRequestCall.getRestCall(Globals.bitbucket,
            "/rest/api/1.0/projects/${bitbucketKey}/permissions/users?name=${reportUser}&limit=300")
        def getbitBucketUsersP = new JsonSlurper().parseText(getbitBucketUsers.toString())
        getbitBucketUsersP.values.each {
            if (it.user.name == reportUser)
                addComment += "Component:Bitbucket\nProject Name:${bitbucketKey}\nUser Name: ${it.user.displayName}\nPermissions: ${it.permission}\n-------\n"
            return true
        }
    } catch (Exception e) {
        addComment += "Either project: $bitbucketKey does not exist on Bitbucket or project key is different\n$e\n--------\n"
    }
}