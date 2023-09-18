package com.elm.scriptrunner.validations


import com.opensymphony.workflow.InvalidInputException


if (issue?.resolution?.name == "Resolved" && !issue.fixVersions) {
    throw new InvalidInputException("fixVersions",
        "Fix Version/s is required when specifying Resolution of 'Resolved'")
}
