package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.link.RemoteIssueLinkManager
import com.atlassian.jira.project.Project
import com.opensymphony.workflow.InvalidInputException
import com.elm.scriptrunner.library.CommonUtil

def exeludedProjects = ['STEL','SNBI','LEP','MEWA','DIP','CA','MODE']
def exeludedComponent = ['CA Engine','Sanad Eligibility','CA CRM']

boolean isComponentExecluded = issue.components.find {it->
    (it.name in exeludedComponent)
} ? false : true

def projectCategory  = ComponentAccessor.getProjectManager().getProjectCategoryForProject(CommonUtil.getProjectObjByIssue(issue) as Project)
def execludedProjectCategory =  ['10002', '10003']

boolean isProjectCategoryExecluded = (projectCategory.getId().toString() in execludedProjectCategory)
boolean isProjectExecluded = !(CommonUtil.getProjectKey(issue).toString() in exeludedProjects)

def VerifyRequirementLinkExist (issue) {
    def reqLink = CommonUtil.getCustomFieldValue(issue, 13004)
    def remoteLinks = ComponentAccessor.getComponent(RemoteIssueLinkManager).getRemoteIssueLinksForIssue(issue)
    def reqLinks = reqLink + remoteLinks
    if (reqLinks.size() < 1) {
        throw new InvalidInputException(CommonUtil.getCustomFieldObject(13004).getId().toString(), "Requirements must be linked with user story")
    }
}

log.warn(isProjectExecluded)
log.warn(isProjectCategoryExecluded)
log.warn(isComponentExecluded)


if (isComponentExecluded && isProjectExecluded && isProjectCategoryExecluded){
    VerifyRequirementLinkExist(issue)
}
