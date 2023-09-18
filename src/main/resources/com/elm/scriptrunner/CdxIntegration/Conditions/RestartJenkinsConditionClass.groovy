package com.elm.scriptrunner.CdxIntegration.Conditions

import com.atlassian.jira.issue.Issue
import com.atlassian.jira.plugin.webfragment.conditions.AbstractWebCondition
import com.elm.scriptrunner.library.CommonUtil;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser
import org.springframework.stereotype.Component;

/**
 * a condition that checks if Build Status is Failed or Aborted
 */

@Component
public class RestartJenkinsConditionClass extends AbstractWebCondition{

    @Override
    boolean shouldDisplay(ApplicationUser applicationUser, JiraHelper jiraHelper) {
        Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue")
        def cfBuildStatus = CommonUtil.getCustomFieldValue(currentIssue,13705).toString()
        if (cfBuildStatus in ['Pipeline is Failed', 'Pipeline is Aborted']){

            return true
        }
        return false
    }
}
