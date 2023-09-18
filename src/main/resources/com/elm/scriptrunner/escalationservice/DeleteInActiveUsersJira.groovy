package com.elm.scriptrunner.escalationservice

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.login.LoginManager
import com.atlassian.jira.bc.security.login.LoginInfo
import groovyx.net.http.RESTClient
import org.apache.log4j.Logger
import static groovyx.net.http.ContentType.JSON


def log = Logger.getLogger("com.onresolve.jira.groovy")
def loginManager = ComponentAccessor.getComponentOfType(LoginManager)
def jiraUsers = ComponentAccessor.getGroupManager().getGroup("jira users") //get All users in "Jira Users" Group
def jiraInternalUsers = ComponentAccessor.getGroupManager().getGroup("jira_internal_users")
def users = ComponentAccessor.getUserUtil().getAllUsersInGroups([jiraUsers,jiraInternalUsers])


for (user in users) {
    def lastLoginMillis = loginManager.getLoginInfo(user.name).getLastLoginTime() //get user's last login Time //
    def disable = false
    if (!lastLoginMillis) {
//user has never logged in
        disable = true
    } else {
        Date cutoff = new Date().minus(45)
        Date last = new Date(lastLoginMillis)
        if (last.before(cutoff)) {
            disable = true
        }
    }
    if (disable == true) {
        restClient('https://crowd.elm.sa/crowd//rest/usermanagement/1/', "group/user/direct", ['username':user.username,'groupname':'jira users'])
        log.info "Deleted ${user.name}"
    } else {
        log.warn "Not valid for Delete ${user.name}"
    }
}

def static restClient(def uri, def path, def query) {
    try {

        def client = new RESTClient(uri)
        def res = client.auth.basic("bamboo", "bamboo")
        client.delete(path: path, query: query, contentType: JSON)
    } catch (Exception e) {

    }
}