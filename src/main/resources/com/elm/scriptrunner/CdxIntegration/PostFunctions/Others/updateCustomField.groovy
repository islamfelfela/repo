package com.elm.scriptrunner.CdxIntegration.PostFunctions.Others

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


def subCategory =  CommonUtil.getCustomFieldObject(12009)


/**
 * a post function that updates the Deployment Type CF to None if Jenkins Branch CF is not None
 */
def issueList = CommonUtil.findIssues("""type = Change AND status = Done AND category in ("Product Integration", PS) AND created >= startOfYear() AND Subcategory is EMPTY""",Globals.botUser)

issueList.each {
    def availableOptions = ComponentAccessor.optionsManager.getOptions(subCategory.getRelevantConfig(it))
    def optionToSet = availableOptions.find { it.value == 'Service Enhancement' }
    it.setCustomFieldValue(subCategory, optionToSet)
    ComponentAccessor.getIssueManager().updateIssue(Globals.botUser, it, EventDispatchOption.DO_NOT_DISPATCH, false)
    log.warn(it.key)
}