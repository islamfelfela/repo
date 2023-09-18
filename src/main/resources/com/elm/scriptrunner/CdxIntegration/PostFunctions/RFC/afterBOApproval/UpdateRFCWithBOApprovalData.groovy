package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC.afterBOApproval

import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.DoRequestCall
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.ProductsKeyMap
import com.google.gson.Gson
import groovy.json.JsonSlurper
import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

/**
 * a post function that updates the rfc with approved by & approval date
 */
def updateChange() {
    //def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    postChangeActivity(cFRFCNumber)
}


def postChangeActivity(String rfcNumber) {
    def changeHistoryManager = ComponentAccessor.getChangeHistoryManager()
//        def changeItemUserKey = changeHistoryManager.getAllChangeItems(issue).find{it.field == 'status' && it.froms == ['status','awaiting']}
    def changeItemUserKey = changeHistoryManager.get //getAllChangeItems(issue).findAll{it.field == 'status'}.froms.findAll {it.containsKey(11301) }.last() //findAll{it.fromValues == '11301' } // == ['12000','Awaiting Release']}

    log.warn(changeItemUserKey)
    def userObject = ComponentAccessor.getUserManager().getUserByKey(changeItemUserKey)
    def comment = ComponentAccessor.commentManager.getComments(issue).findAll{it.authorApplicationUser == userObject}.last().body
    log.warn(comment)

    if (comment) {
        def activityRFC = [
                SXApprovalLog: [
                        Counter : "10419",
                        Action : "Approved",
                        Comments : [""],
                        Date: LocalDateTime.now().toString(),
                        Group: "OPM",
                        Operator : "scaliskan",
                        OperatorFullName : "Serkan Caliskan",
                        UniqueKey : "${rfcNumber}"
            ]
        ]

        def resp = HttpRestUtil.SMPost('/SM/9/rest/SXApprovalLog', new Gson().toJson(activityRFC))
    }
}

updateChange()

