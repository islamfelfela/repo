package com.elm.scriptrunner.spring.config

import com.atlassian.jira.plugin.webfragment.conditions.IsIssueReportedByCurrentUserCondition
import com.atlassian.jira.plugin.webfragment.conditions.IsIssueUnresolvedCondition
import com.atlassian.plugins.osgi.javaconfig.configs.beans.PluginAccessorBean
import com.atlassian.sal.api.ApplicationProperties
import com.elm.scriptrunner.CdxIntegration.Conditions.RestartJenkinsConditionClass
import com.elm.scriptrunner.library.PluginProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

import static com.atlassian.plugins.osgi.javaconfig.OsgiServices.*

@Configuration
@Import(PluginAccessorBean)
class MyPluginConfig {

    @Bean
    IsIssueReportedByCurrentUserCondition isIssueReportedByCurrentUserCondition() {
        return importOsgiService(IsIssueReportedByCurrentUserCondition)
    }

    @Bean
    IsIssueUnresolvedCondition isIssueUnresolvedCondition() {
        return importOsgiService(IsIssueUnresolvedCondition)
    }

    @Bean
    ApplicationProperties applicationProperties(){
        return importOsgiService(ApplicationProperties)

    }

    @Bean
    RestartJenkinsConditionClass restartJenkinsConditionClass(){
        return importOsgiService(RestartJenkinsConditionClass)
    }
}