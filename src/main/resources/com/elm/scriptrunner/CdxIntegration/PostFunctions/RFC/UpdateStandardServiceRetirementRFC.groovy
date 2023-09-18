package com.elm.scriptrunner.CdxIntegration.PostFunctions.RFC


import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.google.gson.Gson
import groovy.transform.Field
import java.time.LocalDateTime
import com.atlassian.jira.component.ComponentAccessor

@Field  def getInsightObject = CommonUtil.getInsightCField(issue,14805,'Name')

def updateChange() {
//    def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-645")
    def cFRFCNumber = CommonUtil.getCustomFieldValue(issue, 11311).toString()
    def rfcTitle = issue.key + " : " + "Service-Rollout" + " : " + getInsightObject[0].toString()
    def releaseNoteURL = CommonUtil.getCustomFieldValue(issue,16401)

    putChange(service: "${getInsightObject[0]}",
            issueKey: issue.key,
            assignee: issue.assignee.displayName,
            issueDesc: issue.description,
            sysDate: LocalDateTime.now().toString(),
            title: rfcTitle,
            releaseNote: releaseNoteURL,
            cFRFCNumber: cFRFCNumber)

        postChangeActivity(cFRFCNumber, issue)
}

def putChange(Map m) {
    def affectedCI =  CommonUtil.getInsightAtrributeValueSpecificObject("""objectType = "Service" AND "Name" = "${getInsightObject[0]}" """,1,'ConfigurationItem')
        .first().toString()
    log.warn(affectedCI)
    def cFClassification = CommonUtil.getCustomFieldValue(issue, 12010).toString()

    def rfcData = [
            Change: [
                    AcceptanceSucessCriteria   : ["As Per Release Note"],
                    ChangeImpact               : ["As Per Release Note"],
                    Classification             : cFClassification,
                    Service                    : affectedCI,
                    "elm.chm.srv.dis.name"     : m.service,
                    "elm.service.environment"  : m.envType,
                    elmchmbusinessunit         : "Technology",
                    elmchmoutage               : "No",
                    elmchmkmtransfer           : "No",
                    elmjiraid                  : m.issueKey,
                    "elm.jira.status"          : "Required Information Provided",
                    ChangeType                 : "Retire Service",
                    ChangeAuthorization        : "ctype_dpt",
                    "description.structure"    : [
                            Description       : m.issueDesc,
                            BackoutMethod     : m.issueDesc,
                            ImplementationPlan: m.issueDesc
                    ],
                    header                     : [
                            Category    : "Operational Standard",
                            Phase       : "Change Approval",
                            Subcategory : "Service Retirement",
                            InitiatedBy : m.assignee,
                            PlannedStart: m.sysDate,
                            Status      : "initial",
                            Title       : m.title
                    ]
            ]
    ]

    def resp = HttpRestUtil.SMPut("/SM/9/rest/changes/${m.cFRFCNumber}",new Gson().toJson(rfcData))
    return (resp.body?.object?.Change?.header?.ChangeID)
}

def postChangeActivity(String rfcNumber,def issue) {
    def comment = ComponentAccessor.commentManager.getLastComment(issue)?.body
    if (comment) {
        def activityRFC = [
            activitychange: [
                number     : rfcNumber,
                type       : "Update from Jira",
                operator   : "elmjirauser",
                description: comment
            ]
        ]
//        def activityRFCJson = new JsonBuilder(activityRFC).toString()
//        HttpBuilderUtil.postUtil(restURL, '/SM/9/rest/activitychange', restUser, restPass, activityRFCJson,'JSON')
        def resp = HttpRestUtil.SMPost('/SM/9/rest/activitychange', new Gson().toJson(activityRFC))
    }
}

updateChange()