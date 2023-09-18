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


/**
 * Send announcement for Marketing team not completed feature
 */

@Field def ServiceName_EN =  CommonUtil.getInsightCField(issue,14805,"Name")
@Field def ServiceName_AR =  CommonUtil.getInsightCField(issue,14805,"Service Name AR")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")


def dateFormat(def issue , Long customFieldId){
    def cFValue = CommonUtil.getCustomFieldValue(issue, customFieldId)
    log.warn(cFValue)
    if(cFValue) {
        return (new SimpleDateFormat("dd/MM/yy HH:mm").format(cFValue))
    }
    else {return ("")}
}

DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()
def emailBody = CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Announcement Template" AND "Id" = Announcement_To_Market """,1, 'Email Template').get(0).toString()
def ServiceName_OutageStart = dateFormat(issue, 15302)
def ServiceName_OutageEnd = dateFormat(issue, 15303)

String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
    .replace("XYZ_EN", ServiceName_EN.toString())
    .replace("Start_Time", "${ServiceName_OutageStart}")
    .replace("End_Time", "${ServiceName_OutageEnd}")
    .replace('IssueURL',"""<a href= '${baseUrl}/browse/${issue.key}'>${issue.key}</a>""")
    .replaceAll("&nbsp;", "")

def subject = ""
def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement("HDBO@elm.sa", "",'', issue.summary, wikiMarkup)
