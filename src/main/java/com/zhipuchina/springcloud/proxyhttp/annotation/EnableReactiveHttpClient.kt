package com.zhipuchina.springcloud.proxyhttp.annotation

import com.zhipuchina.springcloud.proxyhttp.HttpClientImportSelector
import org.springframework.context.annotation.Import

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(HttpClientImportSelector::class)
annotation class EnableReactiveHttpClient {
    //todo 选择是否装配负载均衡
}