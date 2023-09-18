package com.elm.scriptrunner.AnnouncementService

import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException
import groovy.transform.Field
import com.atlassian.jira.component.ComponentAccessor


/**
 * validate all required data for sending announcement
 */

def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
ServiceName_EN.each {
    if (it != "All BO Services")
//    def supportPhone = CommonUtil.getInsightCField(issue, 14805, "Call Center")
//    def supportEmail = CommonUtil.getInsightCField(issue, 14805, "Call Center Email")
    def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${it}" """, 1)

//    if (supportPhone == null) {
//        throw new InvalidInputException("supportPhone is missing")
//    }
//
//    if (!supportEmail == null){
//        throw new InvalidInputException("supportEmail is missing")
//    }
        {
            if (emailList.size() == 0) {
                throw new InvalidInputException("No Customer email avaliable for the selected service ${it}")
            }
        }
}
