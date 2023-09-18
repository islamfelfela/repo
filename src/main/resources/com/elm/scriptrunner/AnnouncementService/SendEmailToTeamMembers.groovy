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
 * Send announcement Email to Internal/External clients email address
 */

def assignedTeamCfValue =  CommonUtil.getInsightCField(issue,17365,"Id").get(0).toString()
@Field def ServiceRequestListCF =  CommonUtil.getInsightCField(issue,17363,"Name")
@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")
//def servicesToEmailThem = ServiceName_EN.contains('All BO Services') ? CommonUtil.getInsightCFieldObject(issue, """objectType = "Service" AND "All BO Services" = Yes """, 1)?.name : ServiceName_EN
def memberObject = CommonUtil.getInsightObjectByAttributeValue("""ObjectType = Member AND Team = "${assignedTeamCfValue}" """,1)
log.warn(memberObject)

 