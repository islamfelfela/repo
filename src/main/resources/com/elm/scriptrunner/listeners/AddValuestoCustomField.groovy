package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.ProjectCreatedEvent
import com.atlassian.jira.issue.fields.config.FieldConfig
import com.atlassian.jira.issue.fields.config.FieldConfigScheme
import com.elm.scriptrunner.library.CommonUtil


def event = event as ProjectCreatedEvent
//get All projects list
def projectManager = ComponentAccessor.getProjectManager()
Collection projectsList = projectManager.getProjects()

//get Custom Field
def customFieldManager = ComponentAccessor.getCustomFieldManager()
def customField=customFieldManager.getCustomFieldObject(11708)



List schemes = customField.getConfigurationSchemes()
FieldConfigScheme sc = schemes.get(0)
Map configs = sc.getConfigsByConfig()
FieldConfig config = (FieldConfig) configs.keySet()
    .iterator().next()
def optionsManager =ComponentAccessor.getOptionsManager()


def projectNames=event.project.name
optionsManager.createOption(config, null,new Long(1), projectNames)


log.warn(projectNames)