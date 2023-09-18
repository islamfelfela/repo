package com.elm.scriptrunner.CdxIntegration.PostFunctions


import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals

/**Update customFieldValue with UpdateIssue**/
def mainMethod() {
    //def issue = ComponentAccessor.issueManager.getIssueByCurrentKey("MS-83")
    def cfClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()
    def jqlSearch = "project='${issue.getProjectObject().key}' and fixVersion ='${issue.fixVersions.last()}' and issuetype in (Story,Bug,Technical-Task,Enhancement)"
    def deliveredChangeIssues = CommonUtil.findIssues(jqlSearch, Globals.botUser)

    deliveredChangeIssues.each { it ->
        if (it.issueType.name == 'Bug') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 271)
        } else if (it.issueType.name == 'Story') {
            CommonUtil.transitionIssue(Globals.botUser, it.id, 241)
        }else if (it.issueType.name == 'Technical-Task'){
            CommonUtil.transitionIssue(Globals.botUser, it.id, 751)
        }else if (it.issueType.name == 'Enhancement'){
            CommonUtil.transitionIssue(Globals.botUser, it.id, 241)
        }
    }
}
mainMethod()