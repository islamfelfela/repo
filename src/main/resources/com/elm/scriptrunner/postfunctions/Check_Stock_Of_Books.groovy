package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.issue.util.IssueChangeHolder
import com.atlassian.jira.bc.issue.IssueService
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException


def issue = ComponentAccessor.getIssueManager()
def customFieldManager = ComponentAccessor.getCustomFieldManager()


def user = userManager.getUserByName("khalqahtani")
def jqlSearch = "project = \"TL\""
def issues = CommonUtil.findIssues(jqlSearch,user)

def rField = customFieldManager.getCustomFieldObject("customfield_13103") //field of books list
def bField = customFieldManager.getCustomFieldObject("customfield_13202") //field of IN-STOCK number

def rQ = issue.getIssueByCurrentKey("SUP-3508")
def rQName = rQ.getCustomFieldValue(rField)

//WorkflowManager workflowManager = ComponentAccessor.getWorkflowManager()
//JiraWorkflow workflow = workflowManager.getWorkflow(rQ)

//return workflow.getLinkedStep(rQ.getStatus()).getActions().id

def actionId = 21
def currentUser = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
IssueService issueService = ComponentAccessor.getIssueService()

def b1 = issue.getIssueByCurrentKey("TL-1")
def b1Name = b1.getCustomFieldValue(rField)
def b1Stock = b1.getCustomFieldValue(bField)

def b2 = issue.getIssueByCurrentKey("TL-3")
def b2Name = b2.getCustomFieldValue(rField)
def b2Stock = b2.getCustomFieldValue(bField)

def b3 = issue.getIssueByCurrentKey("TL-4")
def b3Name = b3.getCustomFieldValue(rField)
def b3Stock = b3.getCustomFieldValue(bField)

def b4 = issue.getIssueByCurrentKey("TL-5")
def b4Name = b4.getCustomFieldValue(rField)
def b4Stock = b4.getCustomFieldValue(bField)

def b5 = issue.getIssueByCurrentKey("TL-6")
def b5Name = b5.getCustomFieldValue(rField)
def b5Stock = b5.getCustomFieldValue(bField)

def b6 = issue.getIssueByCurrentKey("TL-7")
def b6Name = b6.getCustomFieldValue(rField)
def b6Stock = b6.getCustomFieldValue(bField)

def b7 = issue.getIssueByCurrentKey("TL-8")
def b7Name = b7.getCustomFieldValue(rField)
def b7Stock = b7.getCustomFieldValue(bField)


IssueChangeHolder changeHolder = new DefaultIssueChangeHolder()


if (rQName == b1Name && b1Stock!=0 ) {
    def v1 = b1Stock
    v1 = v1 - 1
    bField.updateValue(null, b1, new ModifiedValue(b1Stock, v1), changeHolder)
    if (b1Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName ==b2Name && b2Stock!=0) {
    def v2 = b2Stock
    v2 = v2 - 1
    bField.updateValue(null, b2, new ModifiedValue(b2Stock, v2), changeHolder)
    if (b2Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName == b3Name && b3Stock != 0) {
    def v3 = b3Stock
    v3 = v3 - 1
    bField.updateValue(null, b3, new ModifiedValue(b3Stock, v3), changeHolder)
    if (b3Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName == b4Name && b4Stock != 0) {
    def v4 = b4Stock
    v4 = v4 - 1
    bField.updateValue(null, b4, new ModifiedValue(b4Stock, v4), changeHolder)
    if (b4Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName == b5Name && b5Stock != 0) {
    def v5 = b5Stock
    v5 = v5 - 1
    bField.updateValue(null, b5, new ModifiedValue(b5Stock, v5), changeHolder)
    if (b5Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName == b6Name && b6Stock != 0) {
    def v6 = b6Stock
    v6 = v6 - 1
    bField.updateValue(null, b6, new ModifiedValue(b6Stock, v6), changeHolder)
    if (b6Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}else if (rQName == b7Name && b7Stock != 0) {
    def v7 = b7Stock
    v7 = v7 - 1
    bField.updateValue(null, b7, new ModifiedValue(b1Stock, v7), changeHolder)
    if (b7Stock == 0) {
        throw new InvalidInputException("Note:Out Of Stoke.")
    }
}