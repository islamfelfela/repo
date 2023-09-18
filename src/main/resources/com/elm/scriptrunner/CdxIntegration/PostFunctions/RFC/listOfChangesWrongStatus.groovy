package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC

import com.atlassian.jira.user.ApplicationUser
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field

@Field ApplicationUser executingAdmin = CommonUtil.executeScriptWithAdmin("atlassbot")
@Field String restURL = "http://192.168.47.144:13083"  // Caution -- Production Data
@Field String restUser = "elmjirauser"
@Field String restPass = "ELMhpsm@12345"

def getChangeList(){
    def authString = HttpRestUtil.getAuthString(restUser,restPass)
    def jqlSearch = 'type = Change AND status changed to Scheduling after startOfWeek()  AND status not in ("Awaiting Release",Done)'
    def changeIssues = CommonUtil.findIssues(jqlSearch, executingAdmin)
    def changeIds =[]
    def changeStatus
    changeIssues.each {
        def changeId = CommonUtil.getCustomFieldValue(it,11311)
        def resp = HttpRestUtil.SMGet("/SM/9/rest/changes/"+changeId)
        switch (it.status.name){
            case "Scheduling" : changeStatus = 'CAB Approval'
                break
            case "Awaiting more Information" : changeStatus = 'Change Logging'
                break
            case "Awaiting Release" : changeStatus = 'Change Implementation'
                break
            case "Done" : changeStatus = 'Closure'
                break

        }
        if (resp.body.object.Change.header.Phase != changeStatus){
            changeIds << it.key
        }
    }
    return changeIds
}

getChangeList()