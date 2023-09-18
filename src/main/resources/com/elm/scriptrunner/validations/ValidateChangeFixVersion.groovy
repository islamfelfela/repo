package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.fields.FixVersionsField
import com.opensymphony.workflow.InvalidInputException
import webwork.action.ActionContext


def mainMethod(){
    def request = ActionContext.getRequest() as Object
    def fieldManager = ComponentAccessor.getFieldManager()
    def fixVersionField = fieldManager.getField("fixVersions") as FixVersionsField
    def params = request.getParameterMap()
    def fixVersionValue = fixVersionField.getValueFromParams(params)
        fixVersionField.
    if (issue.fixVersions) {
        throw new InvalidInputException("fixVersions",
            "Fix Version/s is required when specifying Resolution of 'Resolved'")
    }
}

def static getFixVersionsList(long projectId){
    return ComponentAccessor.getVersionManager().getVersions(projectId)
}
