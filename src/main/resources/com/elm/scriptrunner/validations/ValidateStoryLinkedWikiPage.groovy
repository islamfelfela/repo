package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException

def mainMethod(){
    def linkMgr = ComponentAccessor.getIssueLinkManager()
    def linkCollections = linkMgr.getLinkCollectionOverrideSecurity(issue).getAllIssues()
    for (link in linkCollections) {
        if (link.issueType.name == "Bug" && !(link.status.name in ['Done', 'Deferred']) && link.priority.name != 'Low') {
            def invalidInputException = new InvalidInputException("Note: You must link this Story to a Wiki Requirement. "
                + linkCollections.findAll {
                it.issueType.name == "Bug" && !(it.status.name in ['Done', 'Deferred'])
            })
            throw invalidInputException
        }
    }
}

mainMethod()