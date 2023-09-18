package com.elm.scriptrunner.postfunctions

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import groovy.transform.Field

//@Field def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("ELMX-6524")
@Field def subcategoryCF = CommonUtil.getCustomFieldObject(12009)
@Field subcategoryCFValue = CommonUtil.getCustomFieldValue(issue,12009).toString()
@Field def optionServiceEnhance =  CommonUtil.getOptionValue(issue,12009,'Service Enhancement')
@Field def optionBugFix =  CommonUtil.getOptionValue(issue,12009,'Bug Fix')

log.warn subcategoryCFValue
log.warn optionServiceEnhance
log.warn optionBugFix

def mainMethod () {

    if (subcategoryCFValue == 'New service') {
        def projectObject = CommonUtil.getProjectObjByIssue(issue)
        def projectComponents = projectObject.components
        log.warn(projectObject.key)
        if (projectComponents?.size() > 0) {
            projectComponents.each {
                def issueList = CommonUtil.findIssues("type = Change AND project = ${projectObject.key} AND Subcategory = 'New service' AND component ='${it.name}'", Globals.botUser)
                if (issueList?.size() > 2) {
                    subcategoryCF.updateValue(null, issue, new ModifiedValue(subcategoryCFValue, optionServiceEnhance), new DefaultIssueChangeHolder())
                    ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
                }
            }
        } else {
            def issueList = CommonUtil.findIssues("type = Change AND project = ${projectObject.key} AND Subcategory = 'New service'", Globals.botUser)
            log.warn issueList
            if (issueList?.size() > 2) {
                subcategoryCF.updateValue(null, issue, new ModifiedValue(subcategoryCFValue, optionServiceEnhance), new DefaultIssueChangeHolder())
                ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
            }
        }
    }

    else if (subcategoryCFValue == 'Service Enhancement') {
        def fixVersion = issue.fixVersions.first().name
        def bugFixIssueList =
            CommonUtil.findIssues("fixVersion = '${fixVersion}' AND type = Problem", Globals.botUser)

        if (bugFixIssueList?.size()>0){
            subcategoryCF.updateValue(null, issue, new ModifiedValue(subcategoryCFValue, optionBugFix), new DefaultIssueChangeHolder())
            ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, issue, EventDispatchOption.DO_NOT_DISPATCH, false)
        }
    }

}

mainMethod()