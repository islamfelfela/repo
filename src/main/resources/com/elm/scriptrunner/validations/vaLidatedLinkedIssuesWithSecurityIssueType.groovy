package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.opensymphony.workflow.InvalidInputException


def issueType = ComponentAccessor.getIssueLinkManager().getLinkCollectionOverrideSecurity(issue)?.allIssues?.getAt(0)?.issueType.id //Get Issue type from the lonk

def test = 10403

if (issueType == test){
    throw new InvalidInputException("No")
}