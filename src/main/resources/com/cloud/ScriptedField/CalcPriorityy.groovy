package com.cloud.ScriptedField

// //get issue object For Testing

// def issue = get("/rest/api/2/issue/SUPPORT-68")
//         .header('Content-Type', 'application/json')
//         .asObject(Map)
//         .body

// logger.info(issue.key as String)

// get custom fields
def customFields = get("/rest/api/2/field")
    .asObject(List)
    .body
    .findAll { (it as Map).custom } as List<Map>

//get customfields object By Name
def financialImpactCfId = customFields.find { it.name == 'What is the financial impact of this issue to GoFetch?' }?.id
def versionsImpactCfId = customFields.find { it.name == 'Is it impacting all GoFetch plan versions (loyalty, GF+, GoFetch Pay)?' }?.id
def clinicsReputationImpactCfId = customFields.find { it.name == 'Does the issue impact our reputation/trust with clinics?' }?.id
def membersReputationImpactCfId = customFields.find { it.name == 'Does it impact our reputation/trust with our members?' }?.id
def usersImpactCfId = customFields.find { it.name == 'How many users do you think are impacted by this issue?' }?.id // {it.id == 'customfield_10102'}?.id
def reporterCountCfId = customFields.find { it.name == 'How many users have reported the issue?' }?.id //{it.id == 'customfield_10103'}?.id
def fixWorkingHoursCfId = customFields.find { it.name == 'How many hours a week are required to support this work-around (including operational and customer support tasks)?' }?.id
def issueLinkedExistanceCfId = customFields.find { it.name == 'Is this issue connected to underlying/structural issues in the platform? ( repeated, reoccurred from the past, related to a similar issue )' }?.id

def PriorityScoreCfId = customFields.find { it.name == 'PriorityScore'}?.id

// def devWorkRequiredCfId = customFields.find { it.name == 'Based on your assessment of the issue, do you believe that the dev team should?' }?.id

def projectKey = "SUPPORT"

if (issue == null || ((Map)issue.fields.project).key != projectKey) {
    logger.info("Wrong Project ${issue.fields.project.key}")
    return
}

//get customfields Values
def financialImpactCfValue = issue?.fields[financialImpactCfId]
def versionsImpactCfValue = issue?.fields[versionsImpactCfId]
def clinicsReputationCfValue = issue.fields[clinicsReputationImpactCfId]
def membersReputationCfValue = issue.fields[membersReputationImpactCfId]
def usersImpactCfValue = issue.fields[usersImpactCfId]
def reporterImpactCfValue = issue.fields[reporterCountCfId]
def fixWorkingHoursCfValue = issue.fields[fixWorkingHoursCfId]
def issueLinkedCfValue = issue.fields[issueLinkedExistanceCfId]


def score = getFinancialImpactValue(financialImpactCfValue) + getreporterImpactCfValue(reporterImpactCfValue) + getclinicsReputationCfValue(clinicsReputationCfValue)  +
    getmembersReputationCfValue(membersReputationCfValue) + getusersImpactCfValue(usersImpactCfValue)  + getfixWorkingHoursCfValue(fixWorkingHoursCfValue) +
    getissueLinkedCfValue(issueLinkedCfValue) + getversionsImpactCfValue(versionsImpactCfValue)

logger.info(score as String)

put("/rest/api/2/issue/${issue.key}")
    .header("Content-Type", "application/json")
    .body([
        fields:[
            (PriorityScoreCfId): score
        ]
    ])
    .asString()

def getusersImpactCfValue(def cFValue){
    int result = 0
    if (cFValue){

        switch(cFValue.value as String){
            case '1' :
                result =  1
                break
            case '1-5' :
                result = 2
                break
            case '5-50' :
                result = 7
                break
            case '50+' :
                result = 10
                break
            default :
                result = 0
        }
    }
    result

}
def getreporterImpactCfValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value as String){
            case '1' :
                result =  1
                break
            case '1-5' :
                result = 5
                break
            case '5-50' :
                result = 10
                break
            case '50+' :
                result = 15
                break
            default :
                result = 0
        }
    }
    result
}
def getclinicsReputationCfValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value as String){
            case 'High (Example - Incorrect Service Tracking, Unable to find a GoFetch+)' :
                result =  15
                break
            case 'Medium (Virtual Card Unable to Be Generated)' :
                result = 10
                break
            case 'Low (Incorrect plan available for pet owner trying to enroll)' :
                result = 5
                break
            case 'No Impact (Users pet profile incorrect)' :
                result = 1
                break
            default :
                result = 0
        }
    }
    result
}
def getmembersReputationCfValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value as String){
            case 'High (Unable to login, unable to take out payment plan, unable to access critical features)' :
                result =  10
                break
            case 'Medium (Unable to renew or update credit card information)' :
                result = 5
                break
            case 'Low (Unable to update my pet information or profile photo)' :
                result = 1
                break
            default :
                result = 0

        }
    }
    result
}
def getFinancialImpactValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value as String){
            case 'High (missing transactions, incorrect amounts) >$5K' :
                result =  15
                break
            case 'Medium (Multi-users unable to sign-up) $1000-$5000' :
                result = 10
                break
            case 'Low (Single user unable to sign-up) - Less than $1K' :
                result = 5
                break
            default :
                result = 0

        }
    }
    result
}
def getfixWorkingHoursCfValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value as String){
            case 'No Work Around Available' :
                result =  15
                break
            case 'High (5hrs or more)' :
                result = 12
                break
            case 'Medium (1-5hrs)' :
                result = 5
                break
            case 'Low (less than 1hr)' :
                result = 2
                break
            case 'No Work Around Necessary' :
                result = 1
                break
            default :
                result = 0
        }
    }
    result
}
def getissueLinkedCfValue(def cFValue){
    int result = 0
    if (cFValue){
        switch(cFValue.value){
            case 'Yes' :
                result =  5
                break
            case 'No' :
                result = 0
                break
            case "I Don’t Know" :
                result = 1
                break
            default :
                result = 0

        }
    }
    result
}
def getversionsImpactCfValue(def cFValue){
    int result = 0
    int resultValue = 0
    cFValue.each{
        if (cFValue){
            logger.info(it.value as String)
            switch(it.value as String){
                case 'GoFetch+' :
                    resultValue =  5
                    break
                case 'GoFetch Pay' :
                    resultValue = 5
                    break
                case 'GoFetch (Loyalty)' :
                    resultValue = 1
                    break
                default :
                    resultValue = 0
            }
            logger.info(result as String)

            result += resultValue
        }
    }
    logger.info("Is it impacting all GoFetch plan versions (loyalty, GF+, GoFetch Pay)? " + result as String)
    result
}


