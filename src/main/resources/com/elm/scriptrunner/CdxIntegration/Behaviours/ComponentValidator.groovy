package com.elm.scriptrunner.CdxIntegration.Behaviours

import com.atlassian.jira.component.ComponentAccessor
import static com.atlassian.jira.issue.IssueFieldConstants.*
import com.opensymphony.workflow.InvalidInputException

/**
 * verify component is configured properly
 */

def componentManager = ComponentAccessor.projectComponentManager
def issueProjectId= issueContext.projectObject.id
def projectComponents=componentManager.findAllForProject(issueProjectId as long)
def cfComponent= getFieldById(getFieldChanged())
def compValue = cfComponent.getFormValue()
def issueType= issueContext.getIssueType().name.toString()

if(projectComponents.size()>0)
{
    if(cfComponent?.getValue()?.isEmpty())
    {
        cfComponent.setRequired(true)
    }else if(issueType=="Change" && compValue instanceof List){

        cfComponent.setError("Only one component can be selected")
    }
    else{
        cfComponent.setRequired(false)
        cfComponent.clearError()
    }
}
