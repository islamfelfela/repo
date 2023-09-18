package com.elm.scriptrunner.Behaviour


/**
 * Story Template to be added to each story
 */

def selectedIssueType = issueContext.getIssueType().name
def desc = getFieldById("description")
def summary = getFieldById("summary")

def sumDefaultValue = """As a <user identity>, I want <desired feature> in order to <goal>"""
        .replaceAll(/    /, '')


def descDefaultValue =
             """h2. Details
              * 1
              * 2
            
            h2. Acceptance Criteria
              * 1
              * 2
            """.replaceAll(/    /, '')

if (issueContext.issueType.name == "Story" && !underlyingIssue?.description  && !underlyingIssue?.summary) {

    desc.setFormValue(descDefaultValue)
    summary.setFormValue(sumDefaultValue)
}


else if(!underlyingIssue?.summary) {
    desc.setFormValue("")
    summary.setFormValue("")
}