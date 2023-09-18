package com.elm.scriptrunner.AnnouncementService

import com.atlassian.jira.bc.issue.IssueService
import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.issue.IssueInputParameters
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.renderer.wysiwyg.converter.DefaultWysiwygConverter
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.Globals
import com.elm.scriptrunner.library.SendJiraEmail
import groovy.transform.Field

import java.text.SimpleDateFormat

@Field def baseUrl = ComponentAccessor.getApplicationProperties().getString("jira.baseurl")

def cFChangeNumber = CommonUtil.getCustomFieldValue(issue, 11311)?.toString()
def CFOutageType = CommonUtil.getCustomFieldValue(issue, 15914)?.toString()
log.warn(CFOutageType)
def CFOutageExist = CommonUtil.getCustomFieldValue(issue, 15304)?.toString()
log.warn(CFOutageExist)

//def Notify_Elm_Customer = "5ee350d8-363e-4211-ac33-1a39814cc760"
//def requestType = CommonUtil.getCustomFieldValue(issue,11202).requestTypeKey.toString()

/**
 * Create announcement Request when a Change approved with outage
 */
if(CFOutageExist == "Yes") {
    updateWrongOutageDate()
    def CFOutageStart = dateFormat(issue, 15302)
    def CFOutageEnd = dateFormat(issue, 15303)
    def CFOutageStart1 = dateFormat(issue, 15920)
    def CFOutageStart2 = dateFormat(issue, 15921)
    def CFOutageStart3 = dateFormat(issue, 15922)
    def CFOutageStart4 = dateFormat(issue, 15923)
    def CFOutageEnd1 = dateFormat(issue, 15915)
    def CFOutageEnd2 = dateFormat(issue, 15918)
    def CFOutageEnd3 = dateFormat(issue, 15917)
    def CFOutageEnd4 = dateFormat(issue, 15919)

    IssueService issueService = ComponentAccessor.getIssueService()
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    issueInputParameters
        .setProjectId(17304) //EOT Project Id
        .setSummary('Change Number: ' + cFChangeNumber)
        .setIssueTypeId('10600') // IT Help issueType
        .setReporterId('bot')

    IssueService.CreateValidationResult createValidationResult = issueService.validateCreate(Globals.botUser, issueInputParameters)
    if (createValidationResult.isValid()) {
        IssueService.IssueResult createResult = issueService.create(Globals.botUser, createValidationResult)
        log.warn(createResult.errorCollection)

        if (createResult.isValid()) {
            def createdIssueObj = createResult.issue
            // def issueManager = ComponentAccessor.getIssueManager()
            CFOutageStart != 'null' ? issueInputParameters.addCustomFieldValue(15302, CFOutageStart) : log.warn('no Value Added')
            CFOutageEnd != 'null' ? issueInputParameters.addCustomFieldValue(15303, CFOutageEnd) : log.warn('no Value Added')
            CFOutageStart1 != 'null' ? issueInputParameters.addCustomFieldValue(15920, CFOutageStart1) : log.warn('no Value Added')
            CFOutageStart2 != 'null' ? issueInputParameters.addCustomFieldValue(15915, CFOutageEnd1) : log.warn('no Value Added')
            CFOutageStart3 != 'null' ? issueInputParameters.addCustomFieldValue(15921, CFOutageStart2) : log.warn('no Value Added')
            CFOutageStart4 != 'null' ? issueInputParameters.addCustomFieldValue(15918, CFOutageEnd2) : log.warn('no Value Added')
            CFOutageEnd1 != 'null' ? issueInputParameters.addCustomFieldValue(15922, CFOutageStart3) : log.warn('no Value Added')
            CFOutageEnd2 != 'null' ? issueInputParameters.addCustomFieldValue(15917, CFOutageEnd3) : log.warn('no Value Added')
            CFOutageEnd3 != 'null' ? issueInputParameters.addCustomFieldValue(15923, CFOutageStart4) : log.warn('no Value Added')
            CFOutageEnd4 != 'null' ? issueInputParameters.addCustomFieldValue(15919, CFOutageEnd4) : log.warn('no Value Added')

            issueInputParameters.addCustomFieldValue(11202,'eot/5ee350d8-363e-4211-ac33-1a39814cc760')
            issueInputParameters.addCustomFieldValue(14805,issue.projectObject.name) //"Zawil Demo"

            CFOutageType == 'Interruption' ? issueInputParameters.addCustomFieldValue(15871,"تنبيه مجدول بعدم استقرار الخدمة متعدد") :
                issueInputParameters.addCustomFieldValue(15871,"تنبيه مجدول بتوقف الخدمة")

            def updateValidationResult = issueService.validateUpdate(Globals.botUser, createResult.issue.id, issueInputParameters)
            assert updateValidationResult.valid: updateValidationResult.errorCollection

            def issueUpdateResult = issueService.update(Globals.botUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
            if (issueUpdateResult.valid) {

                DefaultWysiwygConverter defaultWysiwygConverter = new DefaultWysiwygConverter()

                def emailBody = CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Announcement Template" AND "Id" = Announcement_To_Market """,1, 'Email Template').get(0).toString()
                def ServiceName_OutageStart = dateFormat(createResult.issue, 15302)
                def ServiceName_OutageEnd = dateFormat(createResult.issue, 15303)
                def ServiceName_EN  = CommonUtil.getCustomFieldValue(createResult.issue,14805).getAt(0).name
                String wikiMarkup = defaultWysiwygConverter.convertXHtmlToWikiMarkup(emailBody)
                    .replace("XYZ_EN", ServiceName_EN.toString())
                    .replace("Start_Time", "${ServiceName_OutageStart}")
                    .replace("End_Time", "${ServiceName_OutageEnd}")
                    .replace('IssueURL',"""<a href= '${baseUrl}/browse/${createResult.issue.key}'>${createResult.issue.key}</a>""")
                    .replaceAll("&nbsp;", "")

                def sendJiraEmail = SendJiraEmail.sendEmailToUsersAnnouncement("HDBO@elm.sa,brmteam@elm.sa", "", '',createResult.issue.summary, wikiMarkup)

            }

//            IssueManager issueManager = ComponentAccessor.getIssueManager()
//            issueManager.updateIssue(Globals.botUser, createResult.issue, EventDispatchOption.ISSUE_UPDATED, false)

        } else {
            log.warn("Error while creating the issue." + createResult.errorCollection)
        }
    }
}

/**
 * This function get the proper date format
 */

def dateFormat(def issue , Long customFieldId){
    def cFValue = CommonUtil.getCustomFieldValue(issue, customFieldId)
    if(cFValue) {
        return(new SimpleDateFormat("dd/MMM/yy H:mm").format(CommonUtil.getCustomFieldValue(issue, customFieldId)))
    }
}

/**
 * This function fix the date format on Jira side
 */
def updateWrongOutageDate (){
    def issueService = ComponentAccessor.issueService
    IssueInputParameters issueInputParameters = issueService.newIssueInputParameters()
    def cFOutageDateList = [15915,15917,15918,15919,15920,15921,15922,15923]

    cFOutageDateList.each {
        def cFValue = CommonUtil.getCustomFieldValue(issue, it)?.toString()
        if(cFValue?.contains( '1970-01-01')){
            issueInputParameters.addCustomFieldValue(it,null)
        }else {
            log.warn('Value is ok')
        }
    }
    def updateValidationResult = issueService.validateUpdate(Globals.botUser, issue.id, issueInputParameters)
   assert updateValidationResult.valid: updateValidationResult.errorCollection

    def issueUpdateResult = issueService.update(Globals.botUser, updateValidationResult, EventDispatchOption.ISSUE_UPDATED, false)
    assert issueUpdateResult.valid: issueUpdateResult.errorCollection

}