package com.zhipuchina.springcloud.proxyhttp

import com.fasterxml.jackson.databind.ObjectMapper
import com.zhipuchina.springcloud.proxyhttp.interfaces.BeforeInterceptors
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class DebugBeforeInterceptor(
    @Autowired val objectMapper: ObjectMapper,
) : BeforeInterceptors {

    override fun <S : WebClient.RequestHeadersSpec<S>> invoke(
        request: WebClient.RequestHeadersSpec<S>,
        body: Mono<*>?,
        bodyElementType: Class<*>?
    ): S {
        body?.let {
            objectMapper.writeValueAsString(it.block())
        }
        println("DebugBeforeInterceptor")
        val requestNew = request.header("AppId","1111111111111111")
            .header("Key","22222222222222")
        return request.header("Test", "123456")
    }
}