package com.elm.scriptrunner.AnnouncementService

import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException
import groovy.transform.Field
import com.atlassian.jira.component.ComponentAccessor



/**
 * validate all required data for sending announcement support plan
 */

def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
ServiceName_EN.each {
    def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${it}" """, 1)
    log.warn(emailList)
    if (emailList.size() == 0 ){
        throw new InvalidInputException("No Customer email avaliable for the selected service ${it}")
    }
}
