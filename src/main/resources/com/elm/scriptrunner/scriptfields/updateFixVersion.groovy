package com.elm.scriptrunner.scriptfields


import com.atlassian.jira.bc.project.version.VersionService
import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.Globals

import java.text.SimpleDateFormat

def versionService = ComponentAccessor.getComponent(VersionService)
def projectManager = ComponentAccessor.getProjectManager()

def user = ComponentAccessor.getJiraAuthenticationContext().getLoggedInUser()

def project = projectManager.getProjectByCurrentKey("SPRC")


def versionManager = ComponentAccessor.getVersionManager()
def versionList = versionManager.getVersions(project.id)

versionList.each {
    def versoinObject = versionManager.getVersionsByName(it.name).first()
    versionManager.editVersionDetails(versoinObject, 'SPRC' + it.name, '')
    versionManager.update(versoinObject)
}