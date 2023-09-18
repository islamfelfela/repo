package com.elm.scriptrunner.CdxIntegration.Listeners

import com.atlassian.jira.bc.project.version.VersionService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionReleaseEvent
import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals


/**Update customFieldValue with UpdateIssue**/
def mainMethod() {
//    def cfClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()
    def event = event as VersionReleaseEvent
    event.version.project.name
    def jqlSearch = "project='${event.version.project.name}' and fixVersion ='${event.version.name}' and issuetype in (Story,Bug,Technical-Task,'Security Bug',Enhancement,'Pain Point')"
    def deliveredChangeIssues = CommonUtil.findIssues(jqlSearch, Globals.botUser)
    deliveredChangeIssues.each { it ->
        if (it.issueType.name == 'Bug') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 371) //  241
            log.warn(it.key + it.status.name)
        } else if (it.issueType.name == 'Enhancement') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 411) //  241
            log.warn(it.key + it.status.name)
        } else if (it.issueType.name == 'Story') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 251)
            log.warn(it.key + it.status.name)
        } else if (it.issueType.name == 'Technical-Task') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 811)
            log.warn(it.key + it.status.name)
        } else if (it.issueType.name == 'Security Bug' && it.status.name.toLowerCase() == 'awaiting release') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 221)
            log.warn(it.key + it.status.name)
        } else if (it.issueType.name == 'Pain Point') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 431)
            log.warn(it.key + it.status.name)
        }
    }
}


mainMethod()