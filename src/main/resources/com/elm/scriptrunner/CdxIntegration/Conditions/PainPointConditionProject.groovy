package com.elm.scriptrunner.CdxIntegration.Conditions


/**
 * a condition that checks if the has a customer pain point label on the issue
 */

def labels = issue.getLabels()
if (labels.contains('CustomerPainPoint')){
    passesCondition = true
}