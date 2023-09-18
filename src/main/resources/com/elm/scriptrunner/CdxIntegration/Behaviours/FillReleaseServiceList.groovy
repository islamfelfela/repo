package com.elm.scriptrunner.CdxIntegration.Behaviours

/**
 * using RestEndpoint to get list of release services
 */

def issueId = underlyingIssue?.id
def releaseService = getFieldByName("Jenkins Branch")

        // releaseService.setHelpText("Please select the right Cdx branch, build will be triggered based on selected branch/pipeline. Select None if its no Cdx release")
releaseService.setRequired(true)
releaseService.convertToSingleSelect([
            ajaxOptions: [
                url : getBaseUrl() + "/rest/scriptrunner/latest/custom/getReleaseServiceList?issueId=${issueId}",
                query: true,
                minQueryLength: 4,
                keyInputPeriod: 500,
                formatResponse: "general"
            ]
        ])