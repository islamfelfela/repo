package com.elm.scriptrunner.postfunctions


import com.atlassian.jira.component.ComponentAccessor
import com.onresolve.scriptrunner.canned.jira.utils.ConditionUtils
import com.onresolve.scriptrunner.canned.jira.workflow.postfunctions.CloneIssue
import com.onresolve.scriptrunner.runner.ScriptRunnerImpl

//def projectKey = "SAL" //Replace with the key of the project you want to copy to
def issueKey = "ADAT-44" //Replace with the key of the issue you want to copy


def projectList = ComponentAccessor.projectManager.getProjects().findAll {
    it.projectCategory?.name in ['TDS','PS']  &&
        ! ['ARB','SOQS','ML','ELOG'].contains(it.key)
}
projectList.each {
    //def projectCategory = ComponentAccessor.getProjectManager().getProjectCategoryForProject(projectManager.getProjectObjByName(it.name))
    //log.warn(projectCategory.name)
    def projectToCopyTo = ComponentAccessor.projectManager.getProjectByCurrentKey(it.key)
    def issueToCopy = ComponentAccessor.issueManager.getIssueObject('ADAT-44')

    log.warn(projectToCopyTo.key)

// Block creation of intra-project links
    def blockIntraprojectLinks = '{l -> l.sourceObject.projectObject != l.destinationObject.projectObject}'

    if (!issueToCopy) {
        log.warn("Issue ${issueKey} does not exist")
        return
    }

//Set the creation parameters/inputs (use clone issue but with no link type)
    def inputs = [
        (CloneIssue.FIELD_TARGET_PROJECT)       : projectToCopyTo.key,
        (CloneIssue.FIELD_LINK_TYPE)            : null,
        (ConditionUtils.FIELD_ADDITIONAL_SCRIPT): [
            "checkLink = $blockIntraprojectLinks;",
            ""
        ],
        (CloneIssue.FIELD_SELECTED_FIELDS)      : null, //clone all the fields
        (CloneIssue.SKIP_EPIC_LINKS)            : "true",
    ] as Map<String, Object>
    def executionContext = [issue: issueToCopy] as Map<String, Object>

    def newClonedIssue = ScriptRunnerImpl.scriptRunner.createBean(CloneIssue)
// Execute the clone action with the specified inputs
    def updatedExecutionContext = newClonedIssue.execute(inputs, executionContext)
//The issue has been successfully cloned
    assert updatedExecutionContext.newIssue
}
