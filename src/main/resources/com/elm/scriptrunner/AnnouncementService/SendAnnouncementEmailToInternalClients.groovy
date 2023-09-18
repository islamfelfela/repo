package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.util.json.JSONObject
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.SimpleDateFormat
import groovy.xml.MarkupBuilder


/**
 * Send announcement only for Internal Clients , for Testing purpose
 */


def emailBodyId =  CommonUtil.getInsightCField(issue,15871,"Id").get(0).toString()
@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def servicesToEmailThem = ServiceName_EN.contains('All BO Services') ? CommonUtil.getInsightCFieldObject(issue, """objectType = "Service" AND "All BO Services" = Yes """, 1)?.name : ServiceName_EN
log.warn(servicesToEmailThem)
if (emailBodyId != 'Announcement_Template_08') {
    if (issue.status.name == "Testing") {
        sendEmailSingleOutage(servicesToEmailThem,issue, [issue.reporter.emailAddress], 'reporter')
    } else if (issue.status.name.toLowerCase() == "waiting for approval") {
        def reviewerEmailList = CommonUtil.getCustomFieldValue(issue, 15853)
        sendEmailSingleOutage(servicesToEmailThem, issue, reviewerEmailList, 'reviewer')
    } else {
        sendEmailSingleOutageToClient(servicesToEmailThem, issue, "customer")
    }
} else {
    if (issue.status.name == "Testing") {
        sendEmailMultipleOutage(servicesToEmailThem, issue, [issue.reporter.emailAddress], 'reporter')
    } else if (issue.status.name.toLowerCase() == "waiting for approval") {
        def reviewerEmailList = CommonUtil.getCustomFieldValue(issue, 15853)
        log.warn(reviewerEmailList)
        sendEmailMultipleOutage(servicesToEmailThem, issue, reviewerEmailList, 'reviewer')
    } else {
        sendEmailMultipleOutageToClient(servicesToEmailThem, issue, 'customer')
    }
}

/**
 * format date function
 */

def dateFormat(def issue , Long customFieldId){
    def cFValue = CommonUtil.getCustomFieldValue(issue, customFieldId)
    log.warn(cFValue)
    if(cFValue) {
        return (new SimpleDateFormat("dd/MM/yy HH:mm").format(cFValue))
    }
    else {return ("")}
}

/**
 * send single outage email to test/review
 */
def sendEmailSingleOutage(def serviceList,def issue ,def sendTo , def senderType){
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
    def ServiceName_OutageStart =  dateFormat(issue, 15302)
    def ServiceName_OutageEnd =  dateFormat(issue, 15303)
    def duration = CommonUtil.getCustomFieldValue(issue,15303).getTime() - CommonUtil.getCustomFieldValue(issue,15302).getTime()
    def formatDuration  = DurationFormatUtils.formatDuration(duration, "HH:mm", true)
//    for (int i = 0; i < serviceList.size(); i++) {
        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND Status != "Return for Maintenance" AND Name = "${serviceList[0]}" """,1)
        def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service" = "${serviceObject.id}" """, 1)
        log.warn(ccEmail)
        if (ccEmail) {
            def serviceEn = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
            def serviceAr = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
            def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
            def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
            String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
                .replace("XYZ_EN", serviceEn)
                .replace("XYZ_AR", serviceAr)
                .replace("Start_Time", "${ServiceName_OutageStart}")
                .replace("End_Time", "${ServiceName_OutageEnd}")
                .replace("Duration_Time", "${formatDuration}")
                .replace("Phone_No", "${cCNumberValue}")
                .replace("Supprot_Email", "${cCEmailValue}")
                .replaceAll("&nbsp;", "")

            def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
            if (senderType == 'reviewer') {
                def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
                def issueLink = """<div style = 'text-align:center;font-size:200%'>
                    <p> Announcement waiting your approval, please follow this URL to approve/reject </p>
                         <a href= '${baseUrl}/browse/${issue.key}'>${issue.key}</a></div>"""
                def approverList = sendTo.each {it.emailAddress}.collect()
                log.warn(approverList)
               // sendTo.each {
                    def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",approverList, subject, issueLink + serviceListToSend +   wikiMarkup)
                //}
            }
            else {
//                log.warn('here')
                def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",issue.reporter.emailAddress, subject,serviceListToSend +  wikiMarkup)
            }
        }
    }
//}

/**
 * send multiple outage email to test/review
 */
def sendEmailMultipleOutage(def serviceList, def issue , def sendTo ,def senderType) {
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody = CommonUtil.getInsightCField(issue, 15871, "Email Template").get(0).toString()
    def ServiceName_OutageStart = dateFormat(issue, 15302)
    def ServiceName_OutageEnd = dateFormat(issue, 15302)
    def ServiceName_OutageStart1 = dateFormat(issue, 15920)
    def ServiceName_OutageEnd1 = dateFormat(issue, 15915)
    def ServiceName_OutageStart2 = dateFormat(issue, 15921)
    def ServiceName_OutageEnd2 = dateFormat(issue, 15918)
    def ServiceName_OutageStart3 = dateFormat(issue, 15922)
    def ServiceName_OutageEnd3 = dateFormat(issue, 15917)
    def ServiceName_OutageStart4 = dateFormat(issue, 15923)
    def ServiceName_OutageEnd4 = dateFormat(issue, 15919)

    def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND Status != "Return for Maintenance" AND  Name = "${serviceList[0]}" """,1)
    log.warn(serviceObject)
    def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service" = "${serviceObject.id}" """, 1)
    if (ccEmail) {
            def serviceEn = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
            def serviceAr = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
            def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
            def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
            String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
                .replace("XYZ_EN", serviceEn)
                .replace("XYZ_AR", serviceAr)
                .replace("OutageStart0", "${ServiceName_OutageStart}")
                .replace("OutageEnd0", "${ServiceName_OutageEnd}")
                .replace("OutageStart1", "${ServiceName_OutageStart1}")
                .replace("OutageEnd1", "${ServiceName_OutageEnd1}")
                .replace("OutageStart2", "${ServiceName_OutageStart2}")
                .replace("OutageEnd2", "${ServiceName_OutageEnd2}")
                .replace("OutageStart3", "${ServiceName_OutageStart3}")
                .replace("OutageEnd3", "${ServiceName_OutageEnd3}")
                .replace("OutageStart4", "${ServiceName_OutageStart4}")
                .replace("OutageEnd4", "${ServiceName_OutageEnd4}")
                .replace("Phone_No", "${cCNumberValue}")
                .replace("Supprot_Email", "${cCEmailValue}")
                .replaceAll("&nbsp;", "")

            def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
            if (senderType == 'reviewer') {
                def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
                def issueLink = """<div style = 'text-align:center;font-size:200%'>
                    <p> Announcement waiting your approval, please follow this URL to approve/reject </p>
                         <a href= '${baseUrl}/browse/${issue.key}'>${issue.key}</a></div>"""
                def approverList = sendTo.each {it.emailAddress}.collect()
                log.warn(approverList)
                // sendTo.each {
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",approverList, subject, issueLink + serviceListToSend +   wikiMarkup)
                //}
            } else {
                def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
                log.warn('here')
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa' , issue.reporter.emailAddress,'', subject,serviceListToSend + wikiMarkup)
            }
        }
    }
//}

/**
 * send single outage email to client
 */
def sendEmailSingleOutageToClient(def serviceList,def issue , def senderType){
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
    def ServiceName_OutageStart =  dateFormat(issue, 15302)
    def ServiceName_OutageEnd =  dateFormat(issue, 15303)
    def duration = CommonUtil.getCustomFieldValue(issue,15303).getTime() - CommonUtil.getCustomFieldValue(issue,15302).getTime()
    def formatDuration  = DurationFormatUtils.formatDuration(duration, "HH:mm", true)

    for (int i = 0; i < serviceList.size(); i++) {
        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList[i]}" """,1)
        def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service"  = "${serviceObject.id}" """, 1)
        def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${serviceObject.id}" """, 1)
        def serviceProvider = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'ServiceProvider').getAt(0)
        log.warn(serviceProvider)
        def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
        log.warn(internalEmailList)

        if (ccEmail) {
            def serviceEn = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
            def serviceAr = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
            def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
            def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
            String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
                .replace("XYZ_EN", serviceEn)
                .replace("XYZ_AR", serviceAr)
                .replace("Start_Time", "${ServiceName_OutageStart}")
                .replace("End_Time", "${ServiceName_OutageEnd}")
                .replace("Duration_Time", "${formatDuration}")
                .replace("Phone_No", "${cCNumberValue}")
                .replace("Supprot_Email", "${cCEmailValue}")
                .replaceAll("&nbsp;", "")

            def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
            if (senderType == 'customer') {
                def customerEmailList = ['OPM@elm.sa','BRMTeam@elm.sa']
                emailList.each {
                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                }
                internalEmailList.each {
                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                }
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '',customerEmailList.join(','), subject, wikiMarkup)
            }
        }
    }
}

/**
 * send multiple outage email to client
 */
def sendEmailMultipleOutageToClient(def serviceList, def issue ,def senderType){
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
    def ServiceName_OutageStart =  dateFormat(issue, 15302)
    def ServiceName_OutageEnd =  dateFormat(issue, 15302)
    def ServiceName_OutageStart1 =  dateFormat(issue,15920)
    def ServiceName_OutageEnd1 =  dateFormat(issue,15915)
    def ServiceName_OutageStart2 =  dateFormat(issue,15921)
    def ServiceName_OutageEnd2 =  dateFormat(issue,15918)
    def ServiceName_OutageStart3 =  dateFormat(issue,15922)
    def ServiceName_OutageEnd3 =  dateFormat(issue,15917)
    def ServiceName_OutageStart4 = dateFormat(issue,15923)
    def ServiceName_OutageEnd4 =  dateFormat(issue,15919)

    for (int i = 0; i < serviceList.size(); i++) {
        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND Status != "Return for Maintenance" AND SendEmail = Yes AND Name = "${serviceList[i]}" """, 1)
        if (serviceObject) {
            def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service" = "${serviceObject.id}" """, 1)
            def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${serviceObject.id}" """, 1)
            def serviceProvider = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'ServiceProvider').getAt(0)
            log.warn(serviceProvider)
            def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
            log.warn(internalEmailList)
            if (ccEmail) {
                def serviceEn = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
                def serviceAr = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
                def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
                def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
                String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
                    .replace("XYZ_EN", serviceEn)
                    .replace("XYZ_AR", serviceAr)
                    .replace("OutageStart0", "${ServiceName_OutageStart}")
                    .replace("OutageEnd0", "${ServiceName_OutageEnd}")
                    .replace("OutageStart1", "${ServiceName_OutageStart1}")
                    .replace("OutageEnd1", "${ServiceName_OutageEnd1}")
                    .replace("OutageStart2", "${ServiceName_OutageStart2}")
                    .replace("OutageEnd2", "${ServiceName_OutageEnd2}")
                    .replace("OutageStart3", "${ServiceName_OutageStart3}")
                    .replace("OutageEnd3", "${ServiceName_OutageEnd3}")
                    .replace("OutageStart4", "${ServiceName_OutageStart4}")
                    .replace("OutageEnd4", "${ServiceName_OutageEnd4}")
                    .replace("Phone_No", "${cCNumberValue}")
                    .replace("Supprot_Email", "${cCEmailValue}")
                    .replaceAll("&nbsp;", "")

                def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
                if (senderType == 'customer') {
                    def customerEmailList = ['OPM@elm.sa','BRMTeam@elm.sa']
                    emailList.each {
                        customerEmailList << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                    }
                    internalEmailList.each {
                        customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                    }
                    def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '', customerEmailList.join(','), subject, wikiMarkup)
                }
            }
        }
    }
}

//def sendEmailSingleOutageToInternalClient(def serviceList,def issue , def senderType){
//    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
//    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
//    def ServiceName_OutageStart =  dateFormat(issue, 15302)
//    def ServiceName_OutageEnd =  dateFormat(issue, 15303)
//    def duration = CommonUtil.getCustomFieldValue(issue,15303).getTime() - CommonUtil.getCustomFieldValue(issue,15302).getTime()
//    def formatDuration  = DurationFormatUtils.formatDuration(duration, "HH:mm", true)
//
//    def serviceEn = []
//    def serviceAr = []
//    def customerEmailList = ['OPM@elm.sa','BRMTeam@elm.sa']
//
//    for (int i = 0; i < serviceList.size(); i++) {
//        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList[i]}" """,1)
////        def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service"  = "${serviceObject.id}" """, 1)
////        def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${serviceObject.id}" """, 1)
//        def serviceProvider = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'ServiceProvider').getAt(0)
//        log.warn(serviceProvider)
//        def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
//        log.warn(internalEmailList)
//        if (internalEmailList) {
//            serviceEn  << CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
//            serviceAr << CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
////            def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
////            def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
//
//            def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
//            if (senderType == 'customer') {
//
//                internalEmailList.each {
//                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                }
//            }
//        }
//    }
//    def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
//    String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
//            .replace("XYZ_EN", serviceEn.join('-'))
//            .replace("XYZ_AR", serviceAr.join('-'))
//            .replace("Start_Time", "${ServiceName_OutageStart}")
//            .replace("End_Time", "${ServiceName_OutageEnd}")
//            .replace("Duration_Time", "${formatDuration}")
////            .replace("Phone_No", "${cCNumberValue}")
////            .replace("Supprot_Email", "${cCEmailValue}")
//            .replaceAll("&nbsp;", "")
//
//    def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '',customerEmailList.join(','), subject, wikiMarkup)
//
//}
//
//def sendEmailMultipleOutageToInternalClient(def serviceList, def issue ,def senderType){
//    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
//    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
//    def ServiceName_OutageStart =  dateFormat(issue, 15302)
//    def ServiceName_OutageEnd =  dateFormat(issue, 15302)
//    def ServiceName_OutageStart1 =  dateFormat(issue,15920)
//    def ServiceName_OutageEnd1 =  dateFormat(issue,15915)
//    def ServiceName_OutageStart2 =  dateFormat(issue,15921)
//    def ServiceName_OutageEnd2 =  dateFormat(issue,15918)
//    def ServiceName_OutageStart3 =  dateFormat(issue,15922)
//    def ServiceName_OutageEnd3 =  dateFormat(issue,15917)
//    def ServiceName_OutageStart4 = dateFormat(issue,15923)
//    def ServiceName_OutageEnd4 =  dateFormat(issue,15919)
//
//    for (int i = 0; i < serviceList.size(); i++) {
//        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND Status != "Return for Maintenance" AND SendEmail = Yes AND Name = "${serviceList[i]}" """, 1)
//        if (serviceObject) {
//            def ccEmail = CommonUtil.getInsightCFieldObject(issue, """objectType = CustomerCallNumbers And "Service" = "${serviceObject.id}" """, 1)
//            def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${serviceObject.id}" """, 1)
//            def serviceProvider = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'ServiceProvider').getAt(0)
//            log.warn(serviceProvider)
//            def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
//            log.warn(internalEmailList)
//            if (ccEmail) {
//                def serviceEn = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Name')?.first()?.toString()
//                def serviceAr = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'Service Name AR')?.first()?.toString()
//                def cCEmailValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Email')?.first()
//                def cCNumberValue = CommonUtil.getInsightCFValueSpecificAttribute(ccEmail[0].id, 'Mobile')?.first()
//                String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
//                        .replace("XYZ_EN", serviceEn)
//                        .replace("XYZ_AR", serviceAr)
//                        .replace("OutageStart0", "${ServiceName_OutageStart}")
//                        .replace("OutageEnd0", "${ServiceName_OutageEnd}")
//                        .replace("OutageStart1", "${ServiceName_OutageStart1}")
//                        .replace("OutageEnd1", "${ServiceName_OutageEnd1}")
//                        .replace("OutageStart2", "${ServiceName_OutageStart2}")
//                        .replace("OutageEnd2", "${ServiceName_OutageEnd2}")
//                        .replace("OutageStart3", "${ServiceName_OutageStart3}")
//                        .replace("OutageEnd3", "${ServiceName_OutageEnd3}")
//                        .replace("OutageStart4", "${ServiceName_OutageStart4}")
//                        .replace("OutageEnd4", "${ServiceName_OutageEnd4}")
//                        .replace("Phone_No", "${cCNumberValue}")
//                        .replace("Supprot_Email", "${cCEmailValue}")
//                        .replaceAll("&nbsp;", "")
//
//                def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
//                if (senderType == 'customer') {
//                    def customerEmailList = []
//                    emailList.each {
//                        customerEmailList << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                    }
//                    internalEmailList.each {
//                        customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                    }
//                    def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '', customerEmailList.join(','), subject, wikiMarkup)
//                }
//            }
//        }
//    }
//}

