package com.elm.scriptrunner.escalationservice

import com.atlassian.applinks.api.ApplicationLinkService
import com.atlassian.applinks.api.application.bitbucket.BitbucketApplicationType
import com.atlassian.sal.api.component.ComponentLocator
import com.atlassian.sal.api.net.Request
import com.atlassian.sal.api.net.Response
import com.atlassian.sal.api.net.ResponseException
import com.atlassian.sal.api.net.ResponseHandler
import groovy.json.JsonSlurper
import kong.unirest.HttpResponse
import kong.unirest.Unirest


def appLinkService = ComponentLocator.getComponent(ApplicationLinkService)
def appLinkName = appLinkService.getPrimaryApplicationLink(BitbucketApplicationType)
def limitsize = 200
def start = 0
assert appLinkName
log.debug(appLinkName)
def Users = []

def isLast = false

while (!isLast) {
    def restCallURL = "rest/api/1.0/admin/groups/more-members?context=bitbucket-users&start=${start}&limit=${limitsize}"
    def authenticatedRequestFactory = appLinkName.createAuthenticatedRequestFactory()
    def result = authenticatedRequestFactory.createRequest(Request.MethodType.GET, restCallURL)
        .addHeader("Content-Type", "application/json")
        .execute(new ResponseHandler<Response>() {
            @Override
            void handle(Response response) throws ResponseException {
                if (response.statusCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception(response.getResponseBodyAsString())
                } else {
                    def disable = false
                    def slurped = new JsonSlurper().parseText(response.getResponseBodyAsString())
                    isLast = slurped.isLastPage
                    def slugs = slurped.values
                    slugs.each {
                        if (!it.lastAuthenticationTimestamp) {
                            Users << it.slug
                        } else {
                            Date cutoff = new Date().minus(30)
                            Date last = new Date(it.lastAuthenticationTimestamp)
                            if (last.before(cutoff)) {
                                Users << it.slug
                            }
                        }
                    }
                    start += 200
                }
            }
        })
}

for (user in Users){
    HttpResponse<String> response = Unirest.delete("https://crowd.elm.sa/crowd/rest/usermanagement/1/group/user/direct?username=${user}&groupname=bitbucket-users")
            .header("Authorization", "Basic YmFtYm9vOmJhbWJvbw==")
            .header("Cookie", "JSESSIONID=E0828043C3FE0ED24D3C1521BEED8C17")
            .asString()

}

