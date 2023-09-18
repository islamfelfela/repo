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
 * this script Testing purpose
 */

def emailBodyId =  CommonUtil.getInsightCField(issue,15871,"Id").get(0).toString()
@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def servicesToEmailThem = ServiceName_EN.contains('All BO Services') ? CommonUtil.getInsightCFieldObject(issue, """objectType = "Service" AND "All BO Services" = Yes """, 1)?.name : ServiceName_EN
log.warn(servicesToEmailThem)

if (emailBodyId in['Announcement_Template_02','Announcement_Template_01']){
    if (issue.status.name == "Testing") {
        sendEmailSingleOutage(servicesToEmailThem,issue, [issue.reporter.emailAddress], 'reporter')
    } else if (issue.status.name.toLowerCase() == "waiting for approval") {
        def reviewerEmailList = CommonUtil.getCustomFieldValue(issue, 15853)
        sendEmailSingleOutage(servicesToEmailThem, issue, reviewerEmailList, 'reviewer')
    } else {
        sendEmailSingleOutageToClient(servicesToEmailThem, issue, "customer")
        sendEmailSingleOutageToInternalClient(servicesToEmailThem, issue, 'customer', emailBodyId)
    }
}
else if(emailBodyId == 'Announcement_Template_08') {
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
else {
    if (issue.status.name == "Testing") {
        sendEmailSingleOutage(servicesToEmailThem,issue, [issue.reporter.emailAddress], 'reporter')
    } else if (issue.status.name.toLowerCase() == "waiting for approval") {
        def reviewerEmailList = CommonUtil.getCustomFieldValue(issue, 15853)
        sendEmailSingleOutage(servicesToEmailThem, issue, reviewerEmailList, 'reviewer')
    } else {
        sendEmailSingleOutageToClientWithInternal(servicesToEmailThem, issue, "customer")
    }
}

def dateFormat(def issue , Long customfieldId){
    def cFValue = CommonUtil.getCustomFieldValue(issue, customfieldId)
    log.warn(cFValue)
    if(cFValue) {
        return (new SimpleDateFormat("dd/MM/yy HH:mm").format(cFValue))
    }
    else {return ("")}
}

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
            def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",'mmojahed@elm.sa', subject, issueLink + serviceListToSend +   wikiMarkup)
            //}
        }
        else {
//                log.warn('here')
            def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
            def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",'mmojahed@elm.sa', subject,serviceListToSend +  wikiMarkup)
        }
    }
}
//}

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
            def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', "",'mmojahed@elm.sa', subject, issueLink + serviceListToSend +   wikiMarkup)
            //}
        } else {
            def serviceListToSend = """<div style = 'text-align:center;font-size:200%'>This Email will be sent to the following Services ${serviceList}</div>"""
            log.warn('here')
            def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa' , 'mmojahed@elm.sa','', subject,serviceListToSend + wikiMarkup)
        }
    }
}
//}

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
//        def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
//        log.warn(internalEmailList)

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
//                internalEmailList.each {
//                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                }
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '','mmojahed@elm.sa', subject, wikiMarkup)
            }
        }
    }
}

def sendEmailSingleOutageToClientWithInternal(def serviceList,def issue , def senderType){
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
//                emailList.each {
//                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                }
                internalEmailList.each {
                    customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                }
                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '','mmojahed@elm.sa', subject, wikiMarkup)
            }
        }
    }
}

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
//            def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
//            log.warn(internalEmailList)
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
//                    internalEmailList.each {
//                        customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
//                    }
                    def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '', 'mmojahed@elm.sa', subject, wikiMarkup)
                }
            }
        }
    }
}

def sendEmailSingleOutageToInternalClient(def serviceList,def issue , def senderType ,def templateType){
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
//    def emailBody =  CommonUtil.getInsightCField(issue,15871,"Email Template").get(0).toString()
    def serviceName_OutageStart =  dateFormat(issue, 15302)
    def serviceName_OutageEnd =  dateFormat(issue, 15303)
    def duration = CommonUtil.getCustomFieldValue(issue,15303).getTime() - CommonUtil.getCustomFieldValue(issue,15302).getTime()
    def formatDuration  = DurationFormatUtils.formatDuration(duration, "HH:mm", true)
    log.warn(formatDuration)
    def customerEmailList = ['OPM@elm.sa','BRMTeam@elm.sa']
    for (int i = 0; i < serviceList.size(); i++) {
        def serviceObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList[i]}" """, 1)
        def serviceProvider = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject.id, 'ServiceProvider').getAt(0)
//        log.warn(serviceProvider)
        def internalEmailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Internal Stakholders" AND "ServiceProvider" = "${serviceProvider}" """, 1)
//        log.warn(internalEmailList)
        if (internalEmailList) {
            def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
            if (senderType == 'customer') {
                internalEmailList.each {
                    customerEmailList << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
                }
            }
        }
    }
    def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
    log.warn(customerEmailList.unique(true).join(','))

    if(templateType = 'Announcement_Template_01') {
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '', 'mmojahed@elm.sa', subject, plannedServiceOutageAnnouncementInHtmlFormat(
                serviceName_OutageStart:serviceName_OutageStart,
                serviceName_OutageEnd:serviceName_OutageEnd,
                formatDuration:formatDuration,
                serviceList))
    }else if (templateType = 'Announcement_Template_02'){
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '', 'mmojahed@elm.sa', subject, plannedServicesInstabilityAnnouncementInHtmlFormat(
                serviceName_OutageStart:serviceName_OutageStart,
                serviceName_OutageEnd:serviceName_OutageEnd,
                formatDuration:formatDuration,
                serviceList))

    }
}

static def plannedServiceOutageAnnouncementInHtmlFormat(Map m, def serviceList ) {
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)

    html.style(type: "text/css",
            '''
                    #myTable{
                        border: 1px solid white;
                        align: center;
                        border-collapse: collapse;
                    }
                    #txt{
                        font-size:21px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                        color:#026CB6;
                    }
                    #txtParagraph{
                        font-size:14px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                    }
                    #txtParagraphColored{
                        font-size:14px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                        background-color:#BDD6EE;
                    }
                    #internalLeftTable{
                        border: 1px solid ;
                        padding: 1px;
                        text-align: left;
                        height: 180px
                    }
                    #internalRightTable{
                        border: 1px solid ;
                        padding: 1px;
                        text-align: right;
                        height: 180px;
                    }
                    th, td{
                      padding: 2px;
                }
                  th{
                        background-color: #04AA6D;
                        color: white;
                }
                #leftText45{
                        text-align:left;
                        border-collapse:collapse;
                }
                #rightText45{
                        text-align:right;
                        border-collapse:collapse;
                        dir:RTL;
                }
            ''')
    html.div(style:'overflow-x:auto;') {
        table('id': 'myTable') {
            tr('id': 'txt') {
                td('id': 'leftText45') {
                    p('Planned Service Outage Announcement')
                }
                td('id': 'rightText45') {
                    p('تنبيه مجدول بتوقف الخدمة')
                }

            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Dear Customer,')
                }
                td('id': 'rightText45') {
                    p('عزيزي العميل،')
                }
            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Out of our efforts to enhance our services, we would like to inform you that there will be an outage for the service, as below :')
                }
                td('id': 'rightText45') {
                    p(': نعمل على تطوير أنظمتنا لخدمتكم بشكل أفضل، ونفيدكم بأن خدماتنا ستكون غير متاحة حسب التفاصيل أدناه')
                }
            }
            tr() {
                td('id': 'leftText45') {
                    p('Planned Outage Timetable :')
                }
                td('id': 'rightText45') {
                    p(': الجدول الزمني للتوقف')
                }
            }
            tr() {
                td('id': 'leftText45') {
                    table() {
                        tr('id':'txtParagraph') {
                            td(style: 'text-align:left;', 'Start Time:')
                            td(m.serviceName_OutageStart)
                        }
                        tr() {
                            td(style: 'text-align:left;', 'End Time:')
                            td(style: 'text-align:left;', m.serviceName_OutageEnd)
                        }
                        tr('id':'txtParagraph') {
                            td(style: 'text-align:left;', 'Duration')
                            td(style: 'text-align:left;', m.formatDuration)
                        }
                    }
                }
                td('id': 'rightText45') {
                    table() {
                        tr('id':'txtParagraph') {
                            td(style: 'text-align:right;',m.serviceName_OutageStart)
                            td(style: 'text-align:right;', ': وقت بدء التوقف')
                        }
                        tr(){
                            td(m.serviceName_OutageEnd)
                            td(style: 'text-align:right;', ': الوقت المتوقع لانتهاء التوقف')
                        }
                        tr('id':'txtParagraph') {
                            td(style: 'text-align:right;', m.formatDuration)
                            td(style: 'text-align:right', ': المده')
                        }
                    }
                }
            }
            tr {
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:left; border-collapse:collapse; width:20%') {
                                p('Affected services')
                            }
                            td {
                                table('id': 'internalLeftTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                                def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
                                                String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
                                                String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Name')?.first()?.toString()
                                                String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Name')?.first()?.toString() : ''
                                                tr {
                                                    td(serviceEnCol1)
                                                    td(serviceEnCol2)
                                                    td(serviceEnCol3)
                                                    i += 3
                                                }
                                            }
                                        } else {
                                            def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:right; border-collapse:collapse; width:45%; dir:RTL;') {
                                table('id': 'internalRightTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                                def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
                                                String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
                                                String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Service Name AR')?.first()?.toString()
                                                String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Service Name AR')?.first()?.toString() : ''
                                                tr {
                                                    td(serviceEnCol1)
                                                    td(serviceEnCol2)
                                                    td(serviceEnCol3)
                                                    i += 3
                                                }
                                            }
                                        } else {
                                            def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                                td(style: 'text-align:right; border-collapse:collapse; width:10%; dir:RTL;') {
                                    p('الخدمات المتأثره')
                                }
                            }
                        }
                    }
                }
            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    p('If you have any question, or if you continue to experience any problems after the above period,please contact us via :')
//                }
//                td('id': 'rightText45') {
//                    p(': إذا كان لديكم أي استفسارات، أو واجهتكم مشكلة بعد الفترة المذكورة أعلاه، يسعدنا تواصلكم معنا عبر القنوات التالية')
//                }
//            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:left;', 'Phone:')
//                            td('920000356')
//                        }
//                        tr {
//                            td(style: 'text-align:left;', 'Email:')
//                            td('hd@elm.sa')
//                        }
//                    }
//                }
//                td('id': 'rightText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:right;', '920000356')
//                            td(style: 'text-align:right;', ': الهاتف')
//                        }
//                        tr {
//                            td(style: 'text-align:right;', 'hd@elm.sa')
//                            td(style: 'text-align:right;', ': البريد الإلكتروني')
//                        }
//                    }
//                }
//            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('We apologize for this inconvenience and thank you for your understanding.')
                }
                td('id': 'rightText45') {
                    p('شكرًا لتفهمكم، ونعتذر عن أي إزعاج قد يترتب على ذلك.')
                }
            }
        }
    }

    return (writer.toString())
}

static def plannedServicesInstabilityAnnouncementInHtmlFormat(Map m, def serviceList) {
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)

    html.style(type: "text/css",
            '''
                    #myTable{
                        border: 1px solid white;
                        align: center;
                        border-collapse: collapse;
                    }
                    #txt{
                        font-size:21px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                        color:#026CB6;
                    }
                    #txtParagraph{
                        font-size:14px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                    }
                    #txtParagraphColored{
                        font-size:14px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                        background-color:#BDD6EE;
                    }
                    #internalLeftTable{
                        border: 1px solid ;
                        padding: 1px;
                        text-align: left;
                    }
                    #internalRightTable{
                        border: 1px solid ;
                        padding: 1px;
                        text-align: right;
                    }
                    th, td{
                      padding: 2px;
                }
                  th{
                        background-color: #04AA6D;
                        color: white;
                }
                #leftText45{
                        text-align:left;
                        border-collapse:collapse;
                }
                #rightText45{
                        text-align:right;
                        border-collapse:collapse;
                        dir:RTL;
                }
            ''')
    html.div(style:'overflow-x:auto;') {
        table('id': 'myTable') {
            tr('id': 'txt') {
                td('id': 'leftText45') {
                    p('Planned Service Instability Announcement')
                }
                td('id': 'rightText45') {
                    p('تنبيه مجدول بعدم استقرار الخدمات')
                }

            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Dear Customer,')
                }
                td('id': 'rightText45') {
                    p('عزيزي العميل،')
                }
            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p('Out of our efforts to enhance our services, we would like to inform you that there will be an instability for all the services, as below:')
                }
                td('id': 'rightText45') {
                    p(': نعمل على تطوير أنظمتنا لخدمتكم بشكل أفضل، ونفيدكم بأن الخدمات ستكون غير مستقرة حسب التفاصيل أدناه')
                }
            }
            tr('id':'txtParagraph') {
                td('id': 'leftText45') {
                    p(style:'background-color:#BDD6EE','Planned Instability Timetable :')
                }
                td('id': 'rightText45') {
                    p(style:'background-color:#BDD6EE',': الجدول الزمني للتوقف')
                }
            }
            tr {
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:left;', 'Start Time:')
                            td(m.serviceName_OutageStart)
                        }
                        tr {
                            td(style: 'text-align:left;', 'End Time:')
                            td(style: 'text-align:left;', m.serviceName_OutageEnd)
                        }
                        tr {
                            td(style: 'text-align:left;', 'Duration')
                            td(style: 'text-align:left;', m.formatDuration)
                        }
                    }
                }
                td('id': 'rightText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:right;', m.serviceName_OutageStart)
                            td(style: 'text-align:right;', ': وقت بدء التوقف')
                        }
                        tr {
                            td(m.serviceName_OutageEnd)
                            td(style: 'text-align:right;', ': الوقت المتوقع لانتهاء التوقف')
                        }
                        tr {
                            td(style: 'text-align:right;', m.formatDuration)
                            td(style: 'text-align:right', ': المده')
                        }
                    }
                }
            }
            tr {
                td('id': 'leftText45') {
                    table('id': 'txtParagraph') {
                        tr {
                            td(style: 'text-align:left; border-collapse:collapse; width:20%') {
                                p('Affected services')
                            }
                            td {
                                table('id': 'internalLeftTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                                def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
                                                String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
                                                String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Name')?.first()?.toString()
                                                String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Name')?.first()?.toString() : ''
                                                tr {
                                                    td(serviceEnCol1)
                                                    td(serviceEnCol2)
                                                    td(serviceEnCol3)
                                                    i += 3
                                                }
                                            }

                                        } else {
                                            def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Name')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                td('id': 'leftText45') {
                    table('id':'txtParagraph') {
                        tr {
                            td(style: 'text-align:right; border-collapse:collapse; width:45%; dir:RTL;') {
                                table('id': 'internalRightTable') {
                                    if (serviceList != null) {
                                        def i = 0
                                        if (serviceList.size() > 1) {
                                            while (i < serviceList.size() - 1) {
                                                def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                                def serviceObject2 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 1)}" """, 1)
                                                def serviceObject3 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i + 2)}" """, 1)
                                                String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
                                                String serviceEnCol2 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject2.id, 'Service Name AR')?.first()?.toString()
                                                String serviceEnCol3 = serviceObject3 ? CommonUtil.getInsightCFValueSpecificAttribute(serviceObject3.id, 'Service Name AR')?.first()?.toString() : ''
                                                tr {
                                                    td(serviceEnCol1)
                                                    td(serviceEnCol2)
                                                    td(serviceEnCol3)
                                                    i += 3
                                                }
                                            }

                                        } else {
                                            def serviceObject1 = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Service AND "Status" != "Return for Maintenance" AND Name = "${serviceList.getAt(i)}" """, 1)
                                            String serviceEnCol1 = CommonUtil.getInsightCFValueSpecificAttribute(serviceObject1.id, 'Service Name AR')?.first()?.toString()
                                            tr {
                                                td(serviceEnCol1)
                                            }
                                        }
                                    }
                                }
                                td(style: 'text-align:right; border-collapse:collapse; width:10%; dir:RTL;') {
                                    p('الخدمات المتأثره')
                                }
                            }
                        }
                    }
                }
            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    p('If you have any question, or if you continue to experience any problems after the above period,please contact us via :')
//                }
//                td('id': 'rightText45') {
//                    p(': إذا كان لديكم أي استفسارات، أو واجهتكم مشكلة بعد الفترة المذكورة أعلاه، يسعدنا تواصلكم معنا عبر القنوات التالية')
//                }
//            }
//            tr('id':'txtParagraph') {
//                td('id': 'leftText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:left;', 'Phone:')
//                            td('920000356')
//                        }
//                        tr {
//                            td(style: 'text-align:left;', 'Email:')
//                            td('hd@elm.sa')
//                        }
//                    }
//                }
//                td('id': 'rightText45') {
//                    table {
//                        tr {
//                            td(style: 'text-align:right;', '920000356')
//                            td(style: 'text-align:right;', ': الهاتف')
//                        }
//                        tr {
//                            td(style: 'text-align:right;', 'hd@elm.sa')
//                            td(style: 'text-align:right;', ': البريد الإلكتروني')
//                        }
//                    }
//                }
//            }
            tr('id':'txtParagraph'){
                td('id': 'leftText45') {
                    p('We apologize for this inconvenience and thank you for your understanding.')
                }
                td('id': 'rightText45') {
                    p('شكرًا لتفهمكم، ونعتذر عن أي إزعاج قد يترتب على ذلك.')
                }
            }
        }
    }

    return (writer.toString())
}