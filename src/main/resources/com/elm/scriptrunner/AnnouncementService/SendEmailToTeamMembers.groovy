package com.elm.scriptrunner.InsightAutomation

import com.atlassian.jira.component.ComponentAccessor
import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import groovy.transform.Field

//@Field def issue = ComponentAccessor.getIssueManager().getIssueByCurrentKey("JD-933")
def serviceList = CommonUtil.getInsightCFieldObject(issue,"""objectType = Service AND ConfigurationItem Is not Empty""",1)

serviceList.each {
    int status = 437
    int displayName = 297
    int ArabicServiceName = 660
    int businessUnit = 888
    int subBusinessUnit = 899
    int serviceClassification = 889
    int opsOwner = 301
    int service_Owner = 305
    int charge_Code = 401
    int service_Phase = 890
    int environmentType = 891
    int customer = 892
    int serviceConsumer = 893
    int development_by = 894
    int implementation_by = 895
    int business_Group = 656
    int service_Support = 896
    int customerEngageUnit = 897
    int portalAccess = 898
    int serviceType = 668
    int portal_URL = 669
    def affectedCI = CommonUtil.getInsightCFValueSpecificAttribute(it.id, 'ConfigurationItem')
    if (affectedCI) {
//        log.warn(URLEncoder.encode(it.name, "UTF-8"))
        try {
            def serviceJsonData = HttpRestUtil.SMGet("/SM/9/rest/devices/${affectedCI.first()}/${URLEncoder.encode(it.name, "UTF-8")}")?.body?.object

            if (serviceJsonData.has('Device')) {
                def serviceData = serviceJsonData.Device
                serviceData.has('Status') ? storeObjectAttribute(it, status, serviceData.Status) : log.warn('status not exist')
                serviceData.has('DisplayName') ? storeObjectAttribute(it, displayName, serviceData.DisplayName) : log.warn('DisplayName not exist')
            }
        } catch (Exception e) {
            log.warn('Service not found ' + e.message)
        }
    }
}

def storeObjectAttribute (def insightObject, int attributeId, String jsonAttributeValue){
    def updatedServiceCount = 0

    //Initialize insight classes
    Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");
    def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectFacadeClass);
    Class objectTypeAttributeFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade");
    def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(objectTypeAttributeFacadeClass)
    Class objectAttributeBeanFactoryClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory");
    def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(objectAttributeBeanFactoryClass)

    def statusTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(attributeId)
    def statusAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(insightObject, statusTypeAttributeBean, jsonAttributeValue)

    try{
        insightObjectAttributeBean = objectFacade.storeObjectAttributeBean(statusAttributeBean)
        updatedServiceCount +=1
    }catch (Exception e) {
        return
    }
}