package com.elm.scriptrunner.validations

import com.opensymphony.workflow.InvalidInputException

def affectedVersion = issue.getAffectedVersions().size()
log.warn(issue.getAffectedVersions())

if (affectedVersion == 0) {
    def invalidInputException = new InvalidInputException("AffectedVersion is Required, If it is not exist please ask TPM to create one")
    throw invalidInputException
}