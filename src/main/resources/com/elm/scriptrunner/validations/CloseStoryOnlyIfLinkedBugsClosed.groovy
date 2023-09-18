package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException



def mainMethod() {
    def linkMgr = ComponentAccessor.getIssueLinkManager()
    def linkCollections = linkMgr.getLinkCollectionOverrideSecurity(issue).getAllIssues()
    for (link in linkCollections) {
        if (link.issueType.name == "Bug" && !(link.status.name in ['Awaiting Release', 'Done', 'Deferred'])
            && link.priority.name != 'Low'
            && (link.getLabels().find { it.label == 'ToBeDeleted' })?.label != 'ToBeDeleted') {
            log.warn(link.issueType.name + ' ' + link.status.name + ' ' + link.priority.name)
            def invalidInputException = new InvalidInputException("You need to take an action (Close/Defer) for all linked Bugs : "
                + linkCollections.findAll {
                it.issueType.name == "Bug" && !(it.status.name in ['Awaiting Release', 'Done', 'Deferred']) && (it.getLabels().find {
                    it.label == 'ToBeDeleted'
                })
            })
            throw invalidInputException
        }
    }
}

mainMethod()