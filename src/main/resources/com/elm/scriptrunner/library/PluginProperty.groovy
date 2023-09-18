package com.elm.scriptrunner.library

import com.atlassian.core.util.ClassLoaderUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

class PluginProperty {
//    @Value('${sm.url}')
//    private String smUrl
//
//    def logData(){
//        log.warn(smUrl)
//    }

    def pluginData() {
        Properties properties = new Properties()
        InputStream is = ClassLoaderUtils.getResourceAsStream("pluginSetting.properties", this.class)
        log.warn(is.getProperties())
        properties.load(is)
        return properties
    }
}