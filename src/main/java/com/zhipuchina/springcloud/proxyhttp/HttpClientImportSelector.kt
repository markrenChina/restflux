package com.zhipuchina.springcloud.proxyhttp

import com.zhipuchina.springcloud.proxyhttp.config.ProxyHttpConfiguration
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata

class HttpClientImportSelector : ImportSelector {

    override fun selectImports(importingClassMetadata: AnnotationMetadata): Array<String> {
        //todo 通过注解判断装配bean
        return arrayOf(
            //ContextUtil::class.java.name,
            ProxyHttpConfiguration::class.java.name
        )
    }
}