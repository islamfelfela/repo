package com.elm.scriptrunner.operationRequests.dnp

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.IssueManager
import com.atlassian.jira.issue.Issue
import com.atlassian.jira.issue.MutableIssue
import com.atlassian.jira.bc.issue.properties.IssuePropertyService
import com.atlassian.jira.event.type.EventDispatchOption
import com.atlassian.jira.user.ApplicationUser
import com.atlassian.jira.entity.property.EntityPropertyService
import org.apache.commons.lang3.StringUtils

//// When using this script in the script console uncomment the next line:
Issue issue = ComponentAccessor.getIssueManager().getIssueObject("DSR-6")
String formName = "ACR"
def fieldNames = ["Environment[1]", "Connction type[1]", "Source Service[1]", "Server Type/Layer[1]", "Destination Service[1]"]
String noValue = "No value"
ApplicationUser loggedInUser = ComponentAccessor.jiraAuthenticationContext.getLoggedInUser()
IssuePropertyService issuePropertyService = ComponentAccessor.getComponentOfType(IssuePropertyService.class)
IssueManager issueManager = ComponentAccessor.getIssueManager()

Closure updateDescription = { MutableIssue i, String fieldValues ->
    i.setDescription(fieldValues)
    //i.setDescription(i.description + " " + fieldValues)
    issueManager.updateIssue(loggedInUser, i, EventDispatchOption.DO_NOT_DISPATCH, false)
}

Closure getFormIds = { def property, String name ->
    def slurper = new groovy.json.JsonSlurper()
    def parsedJson = slurper.parseText(property.value)
    def ids = []
    parsedJson.forms.each(){ form ->
        String id
        if(form.name.equals(name)){
            id = form.id.toString()
        }
        if(id){
            ids.add(Long.valueOf(id))
        }
    }
    return ids
}

Closure<String> getFieldId = { def property, String fieldName ->
    def slurper = new groovy.json.JsonSlurper()
    def parsedJson = slurper.parseText(property.value)
    for (def question : parsedJson.design.questions){
        if(fieldName.equals(StringUtils.substringBetween(question.toString(), "label=", ","))){
            return question.toString().substring(0, question.toString().indexOf("="))
        }
    }
}

Closure getChoiceValue = { def property, String fieldName, String choiceId ->
    def fieldId = getFieldId(property, fieldName)
    def slurper = new groovy.json.JsonSlurper()
    def parsedJson = slurper.parseText(property.value)
    for (def question : parsedJson.design.questions){
        if(question.key.equals(fieldId)){
            for(def choice : question.value.choices){
                if(choice.id == choiceId){
                    return choice.label
                }
            }
        }
    }
}

Closure<String> getFieldValue = { def property, String fieldName ->
    def slurper = new groovy.json.JsonSlurper()
    def parsedJson = slurper.parseText(property.value)
    String fieldId = getFieldId(property, fieldName)
    String text = ""
    for (def answer : parsedJson.state.answers){
        if(fieldId.equals(answer.key.toString())){
            if(answer.value.text &&  !answer.value.text.toString().equals("")){
                return answer.value.text.toString()
            } else if(answer.value.choices){
                String choiceValues = ""
                for(def choice : answer.value.choices){
                    if(choiceValues.equals("")){
                        choiceValues = getChoiceValue(property, fieldName, choice)
                    } else {
                        choiceValues = choiceValues + ", " + getChoiceValue(property, fieldName, choice)
                    }
                }
                return choiceValues.equals("") ? noValue : choiceValues
            } else if(answer.value.users){
                String userValues = ""
                for(def user : answer.value.users){
                    if(userValues.equals("")){
                        userValues = user.name
                    } else {
                        userValues = userValues + ", " + user.name
                    }
                }
                return userValues.equals("") ? noValue : userValues
            } else if(answer.value.date || answer.value.time){
                String dateTime = ""
                if(answer.value.date){
                    dateTime = dateTime + answer.value.date
                }
                if(answer.value.time){
                    if(answer.value.date){
                        dateTime = dateTime + " " + answer.value.time
                    } else {
                        dateTime = dateTime + answer.value.time
                    }
                }
                return dateTime.equals("") ? noValue : dateTime
            }
        }
    }
    if(!text.equals("")){
        return text
    } else {
        return noValue
    }
}

def allProperties = issuePropertyService.getProperties(loggedInUser, issue.id)
log.warn(allProperties)
def forms = []
for(def property : allProperties){
    if(property.key.equals("proforma.forms")){
        forms = getFormIds(property, formName)
    }
}
log.warn(forms)

String result = ""
for (def formId : forms){
    for(def property : allProperties){
        if(property.key.equals("proforma.forms" + ".i" + formId) && property.value.contains("\"schemaVersion\":8")){
            for(def fieldName : fieldNames) {
                if(result.equals("")){
                    result = getFieldValue(property, fieldName)
                } else {
                    result = result + ", " + getFieldValue(property, fieldName)
                }
            }
        }
    }
}
log.warn(result)
if(!result.equals("")){
    updateDescription((MutableIssue) issue, result)
}