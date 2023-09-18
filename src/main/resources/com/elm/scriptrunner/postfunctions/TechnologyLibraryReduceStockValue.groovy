package com.elm.scriptrunner.postfunctions
import com.atlassian.jira.bc.issue.comment.property.CommentPropertyService
import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.ModifiedValue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder
import com.atlassian.jira.issue.util.IssueChangeHolder
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.util.json.JSONObject
import com.atlassian.jira.web.bean.PagerFilter
import com.onresolve.scriptrunner.runner.customisers.WithPlugin
import groovy.transform.Field
import org.apache.log4j.Logger

@WithPlugin("com.elm.jira.plugia")
def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("TECL-1")
@Field final SD_PUBLIC_COMMENT = "sd.public.comment"
def log = Logger.getLogger("com.onresolve.jira.groovy")
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def stock = customFieldManager.getCustomFieldObject(13202) //field of stock
IssueChangeHolder changeHolder = new DefaultIssueChangeHolder()
def userManager = ComponentAccessor.getUserManager()
def user = userManager.getUserByName("khalqahtani")
def Stocks
def bFieldObject = customFieldManager.getCustomFieldObject(13103) //field of Books
def commentPropertyService = ComponentAccessor.getComponent(CommentPropertyService)


def liberaryBooks=findIssues("Books='${issue.getCustomFieldValue(bFieldObject).toString()}'",user)


MutableIssue liberaryIssue

if(liberaryBooks.size()>0) {
    liberaryIssue = ComponentAccessor.getIssueManager().getIssueByCurrentKey(liberaryBooks.get(0).key.toString())

    Stocks = (double) liberaryIssue.getCustomFieldValue(stock)

}

stock.updateValue(null, liberaryIssue, new ModifiedValue(Stocks, --Stocks), changeHolder)
addCommentToIssue(user, "Kindly collect the book from Kamran Kundi office",issue)



def findIssues(String jqlSearch, ApplicationUser user) {
    def searchService = ComponentAccessor.getComponent(SearchService)
    def issueManager = ComponentAccessor.getIssueManager()
    SearchService.ParseResult parseResult = searchService.parseQuery(user, jqlSearch)
    def searchResult = searchService.search(user, parseResult.getQuery(), PagerFilter.getUnlimitedFilter())
    def results = searchResult.results
    return results
}


def addCommentToIssue(def LogedInUser, def comnts,def issue) {
    CommentManager commentManager = ComponentAccessor.getCommentManager()
    def properties = [(SD_PUBLIC_COMMENT): new JSONObject(["REPORTER": true])]
    commentManager.create(issue, LogedInUser, comnts, null, null, new Date(), properties, true)
}


