package com.elm.scriptrunner.rest

import com.elm.scriptrunner.library.CommonUtil
import com.elm.scriptrunner.library.HttpRestUtil
import com.elm.scriptrunner.library.PluginProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.junit.Test

public  class Test {

        ApplicationContext applicationContext = new AnnotationConfigApplicationContext(PluginProperty.class)
        def bean = applicationContext.getBean(PluginProperty)

}