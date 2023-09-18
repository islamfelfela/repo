package com.elm.scriptrunner.validations

import com.elm.scriptrunner.library.CommonUtil
import com.opensymphony.workflow.InvalidInputException


def riskField = CommonUtil.getCustomFieldValue(issue,13300).toString()
def riskSummaryField = CommonUtil.getCustomFieldValue(issue,12800).toString()

if (riskField == 'null'){
    throw new InvalidInputException("Is There Risk & Issue Field  is required")
}

if (riskField == 'Yes') {
    if (!(riskSummaryField)) {
        throw new InvalidInputException("Risks & Issues Description is required")
    }
}
