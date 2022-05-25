package com.zhipuchina.springcloud.proxyhttp.interfaces

import org.aopalliance.intercept.Interceptor
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

/**
 * 前置拦截器接口
 * @author 任家立
 * @since 1.0.1
 */
interface BeforeInterceptors : Interceptor {

    fun <S : WebClient.RequestHeadersSpec<S>> invoke(request: WebClient.RequestHeadersSpec<S>,body: Mono<*>?= null,bodyElementType: Class<*>?=null): S;
}