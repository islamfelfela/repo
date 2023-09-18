package com.elm.scriptrunner.Insight

import com.atlassian.jira.component.ComponentAccessor

/* The object facade class */
def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"))
def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"))
def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"))
def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"))

int assetObjectTypeId = object.getObjectTypeId()
def OperationMgrAttributeName = "OPM Name"
def businessOperationMgrAttributeName ="Business Operations Owner"
def directorAttributeName ="Director"
def vpNameAttributeName ="VPName"
def managerAttributeName ="Manager"


log.warn(object.toString())

// Asset object type attribute bean
def businessOperationMgrTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, businessOperationMgrAttributeName)
log.warn(businessOperationMgrTypeAttributeBean.toString())


def businessOperationMgrObjectAttributeBean = objectFacade.loadObjectAttributeBean(object.getId(), businessOperationMgrTypeAttributeBean.getId())
log.warn(businessOperationMgrObjectAttributeBean.toString())

// Getting the value of the owner attribute (Network Interface value)
def businessOperationMgrObjectAttributeValue = businessOperationMgrObjectAttributeBean.getObjectAttributeValueBeans()[0].getValue()
log.warn(businessOperationMgrObjectAttributeValue.toString())


def iql = """objectType = Employees AND "UserName" = ${businessOperationMgrObjectAttributeValue.toString()}"""
log.warn(iql)
// Change '8' to your Object Schema ID
def userObject = iqlFacade.findObjectsByIQLAndSchema(1, iql)[0]
int userObjectTypeId = userObject.getObjectTypeId()

// Owner object type attribute bean (Network Interface of the Object Type)
def userObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(userObjectTypeId, managerAttributeName)
log.warn(userObjectTypeAttributeBean.toString())

// Owner object attribute bean (Network Interface of the owner object)
def userObjectAttributeBean = objectFacade.loadObjectAttributeBean(userObject.getId(), userObjectTypeAttributeBean.getId())
log.warn(userObjectAttributeBean.toString())

// Getting the value of the owner attribute (Network Interface value)
def ownerObjectAttributeValue = userObjectAttributeBean.getObjectAttributeValueBeans()[0].getValue()
log.warn(ownerObjectAttributeValue.toString())

def assetObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, OperationMgrAttributeName)

def newAssetObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, assetObjectTypeAttributeBean, ownerObjectAttributeValue)

// Store the object attribute into Insight
try {
    assetObjectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newAssetObjectAttributeBean)
} catch (Exception vie) {
    log.warn("Could not update object attribute due to validation exception:" + vie.getMessage())
}




