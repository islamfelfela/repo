package com.elm.scriptrunner.validations

import com.atlassian.jira.component.ComponentAccessor
import com.atlassian.jira.issue.MutableIssue

def Projects = ['JD', 'LEP', 'KEFQ', 'LEP', 'CA', 'MEWA', 'NATP', 'SAEP', 'MQB', 
                'WHD', 'HIS', 'MRO', 'YAK', 'ERA', 'JAL', 'DO', 'CDXL', 'CDXI', 'MLMTI', 
                'MASUL', 'ALB', 'SEEC', 'MOJ', 'WAR', 'GSP', 'MQM', 'RCC', 'MNA', 'ISP',
                'NLBI', 'SPP', 'KOA', 'UXUI']

(issue.project.key in Projects && !issue.components.empty) || issue.project.key in Projects == false