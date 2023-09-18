package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.RendererManager
import com.atlassian.jira.issue.attachment.FileSystemAttachmentDirectoryAccessor
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.issue.fields.renderer.IssueRenderContext
import com.atlassian.jira.issue.fields.renderer.JiraRendererPlugin
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
 * this script Testing purpose
 */

//@Field  def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("AFS-1")

@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
def servicesToEmailThem = ServiceName_EN.contains('All BO Services') ? CommonUtil.getInsightCFieldObject(issue, """objectType = "Service" AND "All BO Services" = Yes """, 1)?.name : ServiceName_EN
log.warn(servicesToEmailThem)

@Field def emailLogger = Logger.getLogger("com.elm.CustomerSupportEmail")
emailLogger.setLevel(Level.DEBUG)

if (issue.status.name == "Testing") {
    sendEmailSingleOutageToClient(servicesToEmailThem,issue, 'reviewer')
}
else {
    sendEmailSingleOutageToClient(servicesToEmailThem, issue, "customer")
}



static def addCommentToIssue(def LogedInUser, def comnts,def issue) {
    CommentManager commentManager = ComponentAccessor.getCommentManager()
    def properties = [(Globals.SD_PUBLIC_COMMENT): new JSONObject(["internal": true])]
    commentManager.create(issue, LogedInUser, comnts, null, null, new Date(), properties, true)
}

def sendEmailSingleOutageToClient(def serviceList,def issue, def senderType) {
    def textContent = issue.getDescription().replace('\n\r','<br>').replaceAll('#','<li style="dir =RTL;text-align: right">')

    //  def textContent =textContent1.getAt(0)+'<li style="dir =RTL;text-align: right">'+ textContent1.getAt(1)+'</li>'+
    // '<li style="dir =RTL;text-align: right">'+ textContent1.getAt(2)+'</li>'+textContent1.getAt(3)

    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBodyObject = CommonUtil.getInsightObjectByAttributeValue("""objectType = "Announcement Template" AND "Id" = Announcement_Template_09""",1)
    def emailBody  = CommonUtil.getInsightCFValueSpecificAttribute(emailBodyObject.id, 'Email Template')?.first()?.toString()
    String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
        .replace("ServiceName", ServiceName_EN.first().toString())
        .replace('EnhancementType',issue.getSummary())
        .replace('Type','Internal')
        .replace('Date', new SimpleDateFormat("dd/MM/yy HH:mm").format(new Date())) //'12-06-2022'
        .replace('TextHere',textContent)
        .replaceAll("&nbsp;", "")

    def subject = issue.getSummary()


    //log.warn(wikiMarkup)
    if (senderType == 'customer'){
        def emailList = CommonUtil.getInsightCFieldObject(issue, """objectType = "Stakholder Data" AND "Service" = "${serviceList[0]}" """, 1)
        log.warn(emailList)
        def  customerEmailList = []
        emailList.each {
            customerEmailList  << CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'Email').getAt(0).toString()
        }
        customerEmailList << 'aalghamdii@elm.sa'
        log.warn(customerEmailList.join(','))
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('no_reply@elm.sa', '',customerEmailList.join(','), subject, wikiMarkup)
        //}
    }
    else if (senderType == 'reviewer'){
        def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement('mmojahed@elm.sa', "", subject, emailBody)
    }
}

