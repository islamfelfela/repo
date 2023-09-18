

import com.atlassian.jira.bc.issue.search.SearchService
import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException

def customFieldObj = ComponentAccessor.getCustomFieldManager()
def vrmField = customFieldObj.getCustomFieldObject('customfield_14800')

def vCFObject = customFieldObj.getCustomFieldObject('customfield_14701')
def vCFValue = issue.getCustomFieldValue(vCFObject) as int

def rCFObject = customFieldObj.getCustomFieldObject('customfield_14702')
def rCFValue = issue.getCustomFieldValue(rCFObject) as int

def mCFObject = customFieldObj.getCustomFieldObject('customfield_14703')
def mCFValue = issue.getCustomFieldValue(mCFObject) as int


def VRM =     vCFValue  + "." + rCFValue  + "." + mCFValue

//println(VRM)
log.warn((VRM))
def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()
def results = CommonUtil.findIssues("VRM ~ ${VRM}", user)
log.warn(results)
if (results.size() > 0) {
    throw new InvalidInputException("VRM already exist")
}