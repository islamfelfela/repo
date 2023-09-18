package com.elm.scriptrunner.PainPointsWorkflow.Postfunctions

import com.elm.scriptrunner.library.Globals

class calcPriorityValue{

    static double getPriorityValue(def issue, def subTaskSummary){
        double decisionValue = 0

        if (subTaskSummary in [Globals.PPSubTaskSummary.CJ , Globals.PPGroups.CJ]) {
            switch (issue.priority.name) {
                case 'Critical': decisionValue = 25
                    break
                case 'High': decisionValue = 20
                    break
                case 'Medium': decisionValue = 15
                    break
                case 'Low': decisionValue = 10
                    break
                default: decisionValue = 0
            }
        }
        else if (subTaskSummary in [ Globals.PPSubTaskSummary.BRM , Globals.PPGroups.BRM]) {
            switch (issue.priority.name) {
                case 'Critical': decisionValue = 30
                    break
                case 'High': decisionValue = 25
                    break
                case 'Medium': decisionValue = 20
                    break
                case 'Low': decisionValue = 15
                    break
                default: decisionValue = 0
            }
        }
        else if (subTaskSummary in [Globals.PPSubTaskSummary.OPM, Globals.PPGroups.OPM]){
            switch (issue.priority.name) {
                case 'Critical': decisionValue = 45
                    break
                case 'High': decisionValue = 35
                    break
                case 'Medium': decisionValue = 25
                    break
                case 'Low': decisionValue = 15
                    break
                default: decisionValue = 0
            }
        }
        return decisionValue
    }
}