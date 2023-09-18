package com.elm.scriptrunner.library


public class ProductsKeyMap {

    static def projectKey = ""
    def static getWikiKey(def pKey) {
        def jiraKeys = ["ESK","BAS", "EJR", "GACM","ELMX", "HAFT", "NQS", "RAH", "WAS", "WSJ", "WSL","RC"]
        def wikiKeysMap = [ESK:'ESKAN',BAS: 'BASH', ELMX: 'BIL', EJR: 'EJAR',
                           GACM: 'GM', HAFT: 'HAFL', NQS: 'NAQ', RAH: 'RAYAH',
                           WAS: 'WASET', WSJ: 'WAS', WSL: 'WASL', RC: 'RYC']

        if (jiraKeys.contains(pKey)) {
            projectKey = wikiKeysMap.get(pKey)
            return projectKey.toString()
        } else {
            return pKey
        }
    }

    def static getBitbucketKey(def pKey) {
        def bitbucketKeys = ['ESKAN', 'ETL', 'GSP', 'HAFT', 'PFC']
        def bitbucketKeysMap = [ESKAN: 'ESK', ETL: 'ETP', GSP: 'GAC', HAFT: 'HAFL', PFC: 'PC']

        if (bitbucketKeys.contains(pKey)) {
            projectKey = bitbucketKeysMap.get(pKey)
            return projectKey.toString()
        } else {
            return pKey
        }
    }

    def static getBambooKey(def pKey) {
        def bambooKeys = ['ESP', 'MA', 'MQM', 'NQS', 'PFC', 'WAS', 'WSJ', 'YKNL', 'ESK']
        def bambooKeysMap = [ESP: 'EP', ESK: 'ES', MA: 'MM', MQM: 'MUQ', NQS: 'NAQ', PFC: 'PC', WAS: 'WASET', WSJ: 'WAS', YKNL: 'YL']

        if (bambooKeys.contains(pKey)) {
            projectKey = bambooKeysMap.get(pKey)
            return projectKey
        } else {
            return pKey
        }
    }
}
