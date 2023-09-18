package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.mail.Email
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import groovy.xml.MarkupBuilder

import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility


/**
 * this script Testing purpose
 */

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("EOT-180")

@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def servicesToEmailThem = ServiceName_EN.contains('All BO Services') ? CommonUtil.getInsightCFieldObject(issue, """objectType = "Service" AND "All BO Services" = Yes """, 1)?.name : ServiceName_EN
log.warn(servicesToEmailThem)

requestType =  CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()
if (requestType != "85dc77f5-acf0-439e-aa52-0a40a7d2a3d6" && issue.resolution.name != "Done"){
    return
}

if (issue.status.name == "Testing") {
    sendEmailSingleOutageToClient(servicesToEmailThem,issue, 'reviewer')
}
else {
    sendEmailSingleOutageToClient(servicesToEmailThem, issue, "customer")
}

def sendEmailSingleOutageToClient(def serviceList,def issue, def senderType) {
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody = 'Announcement_Support_Plan' //CommonUtil.getInsightCField(issue, 15871, "Email Template").get(0).toString()
    String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
            .replaceAll("&nbsp;", "")
    def subject = 'Announcement_Support_Plan' //CommonUtil.getInsightCField(issue, 15871, "Name").get(0).toString()
    def aM= ComponentAccessor.getAttachmentManager().getAttachments(issue).get(0).id.toString()
    String filePath = getAttachmentFile(issue,aM).getPath()

    if (senderType == 'customer'){
        def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND Status = Active AND "Service" = "${serviceList[0]}" AND SupportPlan = Yes """, 1)
        log.warn(serviceList)
        def  customerEmailList = []
        emailList.each {
            customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
        }
        log.warn(customerEmailList.join(','))
        def sendJiraEmail = sendEmailToUsersAnnouncementWithAttachmentAsItIs('no_reply@elm.sa', "","mmojahed@elm.sa", subject, plannedSupportAnnouncementInHtmlFormat(serviceList), filePath, 'SupportPlan')
    }
    else if (senderType == 'reviewer'){
        def sendJiraEmail = sendEmailToUsersAnnouncementWithAttachmentAsItIs('no_reply@elm.sa', "","mmojahed@elm.sa", subject, plannedSupportAnnouncementInHtmlFormat(serviceList), filePath, 'SupportPlan')
    }
}

static File getAttachmentFile(Issue issue, String attachmentId){
    return ComponentAccessor.getComponent(FileSystemAttachmentDirectoryAccessor).getAttachmentDirectory(issue).listFiles().find({
        File it->
            it.getName().equals(attachmentId)
    })
}

static def plannedSupportAnnouncementInHtmlFormat(def serviceList ) {
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
                        text-align: right;
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
    html.table('id': 'myTable') {
            tr('id': 'txt') {
                p('وثيقة دعم الخدمه')
            }
            tr('id': 'rightText45') {
                p('عزيزي العميل،')

            }
            tr('id': 'rightText45') {
                p('تتقدم لك إدارة تشغيل الأعمال بشركة عِلم بأجمل التحيات،')

            }
            tr('id': 'rightText45') {
                p('كجزء من تطلعات شركة عِلم لرفع مستوى رضا عملائها عن طريق تقديم الدعم التشغيلي. يسرنا أن نقدم لكم وثيقة الدعم بالمرفق، وذلك لتسهيل عملية التواصل مع فريق الدعم لدينا')

            }
            tr('id': 'rightText45') {
                p('في حالة وجود اي استفسار نرجو التواصل عبر:')

            }
            tr('id': 'rightText45') {
                p('البريد الإلكتروني :   brm@elm.sa')
            }
            tr('id': 'rightText45') {
                p('متمنين لكم دوام التوفيق')

            }
        }

    return (writer.toString())
}

def sendEmailToUsersAnnouncementWithAttachmentAsItIs(def emailAddress,def ccemail,def bccEmail, def subject, def body , String attachmentFilePath , String supportFileName) {
    try {
        def bodyPart = new MimeBodyPart()
        def mp = new MimeMultipart("mixed")
        def attFds = new FileDataSource(attachmentFilePath)
        bodyPart.setDataHandler(new DataHandler(attFds))
        bodyPart.setFileName(supportFileName)
        mp.addBodyPart(bodyPart)

        def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()
        if (emailAddress != '' || emailAddress != null) {
            if (mailServer) {
                Email email = new Email(emailAddress)
                email.setMimeType("text/html")
                email.setFrom('ElmAlert@elm.sa')
                email.setFromName('ElmOutage')
                email.setSubject(subject)
                email.setCc(ccemail)
                email.setBcc(bccEmail)
                email.setBody(body)
                email.addHeader("X-Priority", "1 (Highest)")
                email.setMultipart(mp)
                mailServer.send(email)

            } else {
                log.debug("error occured")
            }
        } else {
            log.debug("Email Id is not valid")

        }
    } catch (Exception e) {
        log.debug("Error occurred while sending email \n" + e)
    }
}

