package com.elm.scriptrunner.library

import com.atlassian.jira.user.ApplicationUser


class Globals {
    static ApplicationUser botUser = CommonUtil.executeScriptWithAdmin('atlassbot')
    static ApplicationUser powerUser = CommonUtil.executeScriptWithAdmin()

    /**Atlassian Products**/
    static def wiki = 'Wiki'
    static def bamboo = "Bamboo"
    static def jira = "Jira"
    static def bitbucket = "Bitbucket"
    static def jenkins = "Jenkins"

    static def SD_PUBLIC_COMMENT = "sd.public.comment"

    /**Jira Workflow Status**/
    class Status {
        static String open = 'open'
        static String awaitingQADeploy = "awaiting deploy on qa"
        static String development = "development"
        static String awaitingQACertify = 'awaiting qa certificate'
        static String awaitingPmApproval = "awaiting pm approval"
        static String awaitingStageDeploy = "awaiting stage deployment"
        static String awaitingProdDeploy = "awaiting production deployment"
        static String scheduling = "scheduling"
        static String awaitingRelease = 'awaiting release'
        static String awaitingMoreInformation = 'awaiting more information'
        static String done = 'done'
        static String awaitingTechnicalApproval = 'awaiting technical approval'

    }
    /*** Jenkins Status****/
    class JStatus {
        static String deployOnQA = "deploy-to-qa"
        static String certifyRelease = "qa-certificate"
        static String pmApproval = "pm-approval"
        static String scheduling = "scheduling"
        static String deployOnStaging = "Staging-deployment"
        static String deployOnProd = "production-deployment"
        static String stagingCheck = 'awaiting-staging-check'
        static String ProdCheck = 'awaiting-production-check'
    }

    class RequestTypes {
        static def project_creation = 'sup/fffcbeaf-af7c-4032-b7b8-6d212d8b3080'
        static def applicationAccess = '5a5e2758-50cd-4301-9b84-2ef29bcfa866'
        static def applicationAccessTrainee = 'aa802579-aa11-40fd-88ee-3505e5afcdba'
        static def enviromentSupport = 'a33759da-521c-46ed-81f9-420e966c80b2'
    }

    class PPGroups {
        static def BRM = 'business-relationships'
        static def OPM = 'business_operation'
        static def CJ = 'customer-journey'
    }

    class PPSubTaskSummary {
        static def BRM = 'Business Relationships team Evaluation'
        static def OPM = 'Operation Managers Team Evaluation'
        static def CJ = 'Customer Journey team evaluation'

    }

}