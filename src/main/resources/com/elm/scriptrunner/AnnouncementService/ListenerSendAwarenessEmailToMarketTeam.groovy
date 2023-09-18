package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.issue.IssueEvent
import com.atlassian.jira.event.project.VersionCreateEvent
import com.atlassian.jira.issue.comments.CommentManager
import com.atlassian.jira.util.json.JSONObject
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import org.apache.commons.lang3.time.DurationFormatUtils
import java.text.SimpleDateFormat


/**
 * Send Email to Marketing team members with a specific email template
 */

def event  = event as IssueEvent
//def issueObject  = event.issue

@Field def ServiceName_EN = ComponentAccessor.customFieldManager.getCustomFieldObject(14805)?.getValue(issue)?.getAt(0)?.name // CommonUtil.getInsightCField(issue, 14805, "Name")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")

if (issue.reporter.name == 'bot') {
    DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
    def emailBody = CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Announcement Template" AND "Id" = Announcement_To_Market """, 1, 'Email Template').get(0).toString()
    def ServiceName_OutageStart = dateFormat(issue, 15302)
    def ServiceName_OutageEnd = dateFormat(issue, 15303)



    String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
        .replace("XYZ_EN", ServiceName_EN.toString())
        .replace("Start_Time", "${ServiceName_OutageStart}")
        .replace("End_Time", "${ServiceName_OutageEnd}")
        .replace('IssueURL', """<a href= '${baseUrl}/browse/${issue.key}'>${issue.key}</a>""")
        .replaceAll("&nbsp;", "")

    //def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement("mmojahed@elm.sa", "", issue.summary, wikiMarkup)
}

def dateFormat(def issue, Long customfieldId) {
    def cFValue = ComponentAccessor.customFieldManager.getCustomFieldObject(customfieldId)?.getValue(issue)
    log.warn(cFValue)
    if (cFValue) {
        return (new SimpleDateFormat("dd/MM/yy HH:mm").format(cFValue))
    } else {
        return ("")
    }
}