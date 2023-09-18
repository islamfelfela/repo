package com.elm.scriptrunner.Behaviour


/**
 * required information for creating a change
 */

def descField  = getFieldByName("Fix Versions")
def summaryField = getFieldByName("Description")
def classificationField = getFieldByName("Classification")
def subCategoryField = getFieldByName("Description")
def changeNumberField = getFieldByName("Change number")
def plannedStartField = getFieldByName("Planned Start")
def plannedEndField = getFieldByName("Planned End")


descField.setReadOnly(true)
summaryField.setReadOnly(true)
classificationField.setReadOnly(true)
subCategoryField.setReadOnly(true)
changeNumberField.setReadOnly(true)
plannedStartField.setReadOnly(true)
plannedEndField.setReadOnly(true)