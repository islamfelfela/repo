package com.elm.scriptrunner.CdxIntegration.Behaviours

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.web.bean.PagerFilter

///******* To BE Reviewed ****/////

/**
 * Validate Fix Version Selection
 */

def projectName = underlyingIssue?.projectObject.name.toString()
def issueKey = underlyingIssue?.key.toString()
def relaseService = getFieldByName("Jenkins Branch")
def fixVersion = getFieldById(getFieldChanged())
if (fixVersion.value.size() > 0) {
    fixVersion.value.each {
        def jql = "project ='${projectName}'  and fixVersion = '${it.name.toString()}'  and type = change" +
                " and status not in (Open,Development,'Awaiting Deploy on QA','Awaiting QA Certificate')"
        fixVersion.setHelpText(jql.toString())
        def mappingResults = findIssues(jql)

        if (mappingResults.size() > 0) {
            //    getFieldById("summary").setHelpText(mappingResults.toString())

            fixVersion.setError("You cannot assign issues to fix version : ${it.name.toString()} because this version is under Deployment to Production")

        } else {
            fixVersion.clearError()
        }

    }
} else {
    fixVersion.clearError()
}


def static findIssues(String jqlSearch) {
    ApplicationUser user = ComponentAccessor.getUserManager().getUserByName("bot")
    def searchService = ComponentAccessor.getComponent(SearchService)
    SearchService.ParseResult parseResult = searchService.parseQuery(user, jqlSearch)
    def searchResult = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter())
    def results = searchResult.results.key
    return results
}


