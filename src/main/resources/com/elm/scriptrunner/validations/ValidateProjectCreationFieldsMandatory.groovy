//package com.elm.scriptrunner.validations
//
//import com.atlassian.jira.component.ComponentAccessor
//import com.opensymphony.workflow.InvalidInputException
//
//def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
//def cRequestTypeValue = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(11202).getValue(issue)?.value //Customer Request Type custom filed ID
//def cProjectKey = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(11502)
//def cProjectName = ComponentAccessor.getCustomFieldManager().getCustomFieldObject(11503)
//
//
//if (cRequestTypeValue == "sup/fffcbeaf-af7c-4032-b7b8-6d212d8b3080" && !(issue.getCustomFieldValue(cProjectKey)||issue.getCustomFieldValue(cProjectName))) {
//    cProjectName.propertySet.
//    throw new InvalidInputException("Project Key & Project Name ")
//}
//
//
//import com.onresolve.jira.groovy.user.FormField
//
//FormField cRequestTypeValue = issue.getFieldById("customfield_11202")
//FormField cProjectKey = getFieldById("customfield_11502")
//FormField cProjectName = getFieldById("customfield_11503")
//
//if (cRequestTypeValue.getFormValue() == "sup/fffcbeaf-af7c-4032-b7b8-6d212d8b3080") { //Yes
//    cProjectKey.setHidden(false)
//    cProjectName.setHidden(false)
//}
//else {
//    cProjectKey.setHidden(true)
//    cProjectName.setHidden(true)
//}