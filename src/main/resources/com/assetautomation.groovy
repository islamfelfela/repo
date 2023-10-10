
import com.atlassian.jira.component.ComponentAccessor
import groovy.transform.Field

log.warn("Srart.........")
/* The object facade class */
Class objectFacadeClass = ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade");

def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"))
def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"))
def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"))
def iqlFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.IQLFacade"))

int assetObjectTypeId = object.getObjectTypeId()
def businessOperationMgrAttributeName ="Business Operations Owner"
def opmLeadAttributeName = "OPM Lead"
def pmAttributeName = "Project Manager"
def serviceOwnerAttributeName = "Service Owner"
def managerAttributeName ="Manager"
def directorAttributeName ="Director"
def vpNameAttributeName ="Manager"


log.warn(object.toString())

def businessOperationMgrObjectAttributeValue = getObjectAttributeValue(businessOperationMgrAttributeName,object)
def iqlOpmManager = """objectType = Employees AND "UserName" = "${businessOperationMgrObjectAttributeValue.toString()}" """
def userObject = iqlFacade.findObjectsByIQLAndSchema(1, iqlOpmManager)?.getAt(0)

def pmMgrObjectAttributeValue = getObjectAttributeValue(pmAttributeName,object)
def iqlPmManager = """objectType = Employees AND "UserName" = "${pmMgrObjectAttributeValue.toString()}" """
log.warn(iqlPmManager.toString())
def pmUserObject = iqlFacade.findObjectsByIQLAndSchema(1, iqlPmManager)?.getAt(0)
log.warn(pmUserObject.toString())

//get Leads
def opmLeadObjectAttributeValue = getObjectAttributeValue(managerAttributeName,userObject)
def serviceOwnerObjectAttributeValue = getObjectAttributeValue(managerAttributeName,pmUserObject)
def directorObjectAttributeValue = getObjectAttributeValue(directorAttributeName,pmUserObject)

//def departmentObjectAttributeValue = getObjectAttributeValue(departmentAttributeName,userObject)
//log.warn(departmentObjectAttributeValue.toString())

def iqlDirectorManager = """objectType = Department AND object HAVING inboundReferences("UserName" = "${directorObjectAttributeValue.toString()}") ORDER BY Name DESC """
log.warn(iqlDirectorManager)

def directorUserObject = iqlFacade.findObjectsByIQLAndSchema(1, iqlDirectorManager)?.getAt(0)
def directorObjectForDepartmentAttributeValue = getObjectAttributeValue(directorAttributeName,directorUserObject)

def iqlVpName = """objectType = Employees AND "UserName" = "${directorObjectForDepartmentAttributeValue.toString()}" """
log.warn(iqlVpName.toString())

def vpUserObject = iqlFacade.findObjectsByIQLAndSchema(1, iqlVpName)?.getAt(0)
def vpNameObjectAttributeValue = getObjectAttributeValue(vpNameAttributeName,vpUserObject)
log.warn(vpNameObjectAttributeValue.toString())

def opmLeadObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, opmLeadAttributeName)
def serviceOwnerObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, serviceOwnerAttributeName)
def directorObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, directorAttributeName)
def vpNameObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(assetObjectTypeId, "VPName")

def newOpmLeadObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, opmLeadObjectTypeAttributeBean, opmLeadObjectAttributeValue)
def newServiceOwnerObjectAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, serviceOwnerObjectTypeAttributeBean, serviceOwnerObjectAttributeValue)
def newDirectorObjectTypeAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, directorObjectTypeAttributeBean, directorObjectAttributeValue)
def newVpNameObjectTypeAttributeBean = objectAttributeBeanFactory.createObjectAttributeBeanForObject(object, vpNameObjectTypeAttributeBean, vpNameObjectAttributeValue)

// Store the object attribute into Insight
try {
    opmLeadObjectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newOpmLeadObjectAttributeBean)
    serviceOwnerObjectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newServiceOwnerObjectAttributeBean)
    directorObjectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newDirectorObjectTypeAttributeBean)
    vpNameObjectTypeAttributeBean = objectFacade.storeObjectAttributeBean(newVpNameObjectTypeAttributeBean)

} catch (Exception vie) {
    log.warn("Could not update object attribute due to validation exception: " + vie.getMessage())
}

def getObjectAttributeValue (def attributeName, def userObject ) {
    try {
        def objectFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectFacade"))
        def objectTypeAttributeFacade = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.channel.external.api.facade.ObjectTypeAttributeFacade"))
        def objectAttributeBeanFactory = ComponentAccessor.getOSGiComponentInstanceOfType(ComponentAccessor.getPluginAccessor().getClassLoader().findClass("com.riadalabs.jira.plugins.insight.services.model.factory.ObjectAttributeBeanFactory"))

        int userObjectTypeId = userObject.getObjectTypeId()
        // Owner object type attribute bean
        def userObjectTypeAttributeBean = objectTypeAttributeFacade.loadObjectTypeAttributeBean(userObjectTypeId, attributeName)
//        log.warn(userObjectTypeAttributeBean.toString())

        // Owner object attribute bean (Manager Object)
        def userObjectAttributeBean = objectFacade.loadObjectAttributeBean(userObject.getId(), userObjectTypeAttributeBean.getId())
//        log.warn(userObjectAttributeBean.toString())

        // Getting the value of the owner attribute (ManagerValue value)
        def objectAttributeValue = userObjectAttributeBean.getObjectAttributeValueBeans()[0].getValue()
//        log.warn(objectAttributeValue.toString())

        def jiraUser = ComponentAccessor.userManager.getUserByName(objectAttributeValue as String)
        if (!jiraUser) {
            jiraUser = ComponentAccessor.userManager.getUserByKey(objectAttributeValue as String)

//            log.error "Unable to find jira user with name $objectAttributeValue"
//            return
        }
        return jiraUser.name

    } catch (Exception vie) {
        log.warn("Could not update object not exist: " + vie.getMessage())

    }
}
log.warn("End.........")