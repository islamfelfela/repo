package com.elm.scriptrunner.validations



def Projects = ['KYC', 'MLMTI', 'MASUL', 'BAY', 'WSL']

def cFstoryPoint = customFieldManager.getCustomFieldObject(10006)

issue.project.key in Projects && !issue.getCustomFieldValue(cFstoryPoint)