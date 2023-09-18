package com.elm.scriptrunner.library

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.security.roles.ProjectRoleManager
import com.atlassian.mail.Email
import groovy.util.logging.Log4j

import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeUtility


@Log4j
class SendJiraEmail {
    /***** Project Role ****/
    static def sendEmailToJiraProjectRole(def jiraProjectRole, def jiraProjectKey, def subject, def ccEmail, def emailBody) {
        try {
            def projectRoleManager = ComponentAccessor.getComponent(ProjectRoleManager)
            def projectRole = projectRoleManager.getProjectRole(jiraProjectRole)
            def projectManager = ComponentAccessor.getProjectManager()
            def jiraProject = projectManager.getProjectObjByKey(jiraProjectKey)
            def projectRoleActors = projectRoleManager.getProjectRoleActors(projectRole, jiraProject)
            def projectRoleActorsEmailTo = projectRoleActors?.getApplicationUsers()*.emailAddress.join(",")
            sendEmailToUsers(projectRoleActorsEmailTo, ccEmail, subject, emailBody)
        } catch (Exception e) {
            log.debug('Error occured while sending email to project role\n') + e
        }
    }
    /*Provide email as single email or multiple emails for example email1@emali.com,email2@email.com*/
    static  def sendEmailToUsersAnnouncement(def emailAddress,def ccEmail,def bccEmail, def subject, def body) {
        try {
            def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()
            if (emailAddress != '' || emailAddress != null) {
                if (mailServer) {
                    Email email = new Email(emailAddress)
                    email.setMimeType("text/html")
                    email.setFrom('ElmAlert@elm.sa')
                    email.setFromName('ElmOutage')
                    email.setSubject(subject)
                    email.setCc(ccEmail)
                    email.setBcc(bccEmail)
                    email.setBody(body)
                    email.addHeader("X-Priority", "1 (Highest)")
                    mailServer.send(email)

                } else {
                    log.debug("error occured")
                }
            } else {
                log.debug("Email Id is not valid")

            }
        } catch (Exception e) {
            log.debug("Error occurred while sending email \n" + e)
        }
    }

    static  def sendEmailToUsersAnnouncementWithAttachment(def emailAddress,def ccemail,def bccEmail, def subject, def body , String attachmentFilePath , String supportFileName) {
        try {
            def bodyPart = new MimeBodyPart()
            def mp = new MimeMultipart("mixed")
            def attFds = new FileDataSource(attachmentFilePath)
            bodyPart.setDataHandler(new DataHandler(attFds))
            bodyPart.setFileName(MimeUtility.encodeText("${supportFileName}.pdf"))
            mp.addBodyPart(bodyPart)

            def mailServer = ComponentAccessor.getMailServerManager().getDefaultSMTPMailServer()
            if (emailAddress != '' || emailAddress != null) {
                if (mailServer) {
                    Email email = new Email(emailAddress)
                    email.setMimeType("text/html")
                    email.setFrom('ElmAlert@elm.sa')
                    email.setFromName('ElmOutage')
                    email.setSubject(subject)
                    email.setCc(ccemail)
                    email.setBcc(bccEmail)
                    email.setBody(body)
                    email.addHeader("X-Priority", "1 (Highest)")
                    email.setMultipart(mp)
                    mailServer.send(email)

                } else {
                    log.debug("error occured")
                }
            } else {
                log.debug("Email Id is not valid")

            }
        } catch (Exception e) {
            log.debug("Error occurred while sending email \n" + e)
        }
    }

}
