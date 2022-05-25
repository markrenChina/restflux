package com.zhipuchina.springcloud.proxyhttp.interfaces

import org.aopalliance.intercept.Interceptor
import org.springframework.web.reactive.function.client.WebClient

/**
 * 后置拦截器接口
 * @author 任家立
 * @since 1.0.1
 */
interface AfterInterceptors : Interceptor {

    fun <S : WebClient.ResponseSpec> invoke(response: S): S
}