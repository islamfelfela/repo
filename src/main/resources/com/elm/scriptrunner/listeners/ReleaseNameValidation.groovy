package com.elm.scriptrunner.listeners

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.event.project.VersionReleaseEvent

def event = event as VersionReleaseEvent
Long projectId=event.version.project.id
String versionName=event.version.name.toString()
String updateVersionName=versionName.replaceAll("\\s","")

updateVersion(projectId,versionName,updateVersionName)

def updateVersion(Long projectId,String oldVersionName, String NewVersionName){
    def versionObject= ComponentAccessor.getVersionManager().getVersion(projectId,oldVersionName)
    ComponentAccessor.getVersionManager().editVersionDetails(versionObject,NewVersionName,"")
}

def version="a b c123.1.2/"
version.replaceAll("\\s","").replaceAll("[^a-zA-Z0-9. ]+","")