package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
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

import org.apache.log4j.Logger
import org.apache.log4j.Level

/**
 * send support plan in pdf format to the client manually (user can select a specific email list)
 */

/**
 * get customFields data
 */
@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def servicesToEmailThem = ServiceName_EN
log.warn(servicesToEmailThem)

//requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()
//
//if (requestType != "3626ce98-e921-491a-8022-b5975db01570" && issue.resolution.name != "Done"){
//    return
//}

if (issue.status.name == "Testing") {
    sendEmailSingleOutageToClient(servicesToEmailThem,issue, 'reviewer')
}
else {
    sendEmailSingleOutageToClient(servicesToEmailThem, issue, "customer")
}


/**
 * send single outage email to client
 */
def sendEmailSingleOutageToClient(def serviceList,def issue, def senderType) {
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody = CommonUtil.getInsightCField(issue, 15871, "Email Template").get(0).toString()
    String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
            .replaceAll("&nbsp;", "")
    def subject = CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
    def aM = ComponentAccessor.getAttachmentManager().getAttachments(issue).get(0).id.toString()
    String filePath = getAttachmentFile(issue, aM).getPath()

    if (senderType == 'customer') {
        def customerEmailList = CommonUtil.getCustomFieldValue(issue, 15827)
        log.warn(customerEmailList.toString())
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncementWithAttachment('no_reply@elm.sa', "", customerEmailList, subject, wikiMarkup, filePath, 'SupportPlan')
    } else if (senderType == 'reviewer') {
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncementWithAttachment('no_reply@elm.sa', "", issue.reporter.emailAddress, subject, plannedSupportAnnouncementInHtmlFormat(serviceList), filePath, 'SupportPlan')
    }
}

/**
 * get attachment filePath from jira
 */
static File getAttachmentFile(Issue issue, String attachmentId){
    return ComponentAccessor.getComponent(FileSystemAttachmentDirectoryAccessor).getAttachmentDirectory(issue).listFiles().find({
        File it->
            it.getName().equals(attachmentId)
    })
}

/**
 * send a well formatted email to the users
 */
static def plannedSupportAnnouncementInHtmlFormat(def serviceList ) {
    def writer = new StringWriter()
    def html = new MarkupBuilder(writer)

    html.style(type: "text/css",
            '''
                    #myTable{
                        border: 1px solid white;
                        width: 100%;
                        border-collapse: collapse;
                    }
                    #txt{
                        font-size:21px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        sans-serif;
                        color:#026CB6;
                        text-align: right;
                    }
                    #txtParagraph{
                        font-size:14px;
                        line-height:105%;
                        font-family:"DIN Next LT Arabic";
                        text-align: right;
                        dir:RTL;
                    }
            ''')
    html.div(style:'overflow-x:auto;') {
        table('id': 'myTable') {
            tr('id': 'txt') {
                td('وثيقة دعم الخدمه')
            }
            tr('id':'txtParagraph') {
                td('عزيزي العميل،')
            }
            tr('id':'txtParagraph') {
                td('تتقدم لك إدارة تشغيل الأعمال بشركة عِلم بأجمل التحيات،')
            }
            tr('id':'txtParagraph') {
                td('كجزء من تطلعات شركة عِلم لرفع مستوى رضا عملائها عن طريق تقديم الدعم التشغيلي. يسرنا أن نقدم لكم وثيقة الدعم بالمرفق، وذلك لتسهيل عملية التواصل مع فريق الدعم لدينا')
            }
            tr('id':'txtParagraph') {
                td(': في حالة وجود اي استفسار نرجو التواصل عبر')
            }
            tr('id':'txtParagraph') {
                table() {
                    tr('id':'txtParagraph') {
                        td('brm@elm.sa')
                        td(': البريد الإلكتروني')
                    }
                }
            }
            tr('id':'txtParagraph') {
                td('متمنين لكم دوام التوفيق')
            }
        }
    }
    return (writer.toString())
}
