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
        val data = body?.let {
            objectMapper.writeValueAsString(it.cache().block())
        }
        println("data = $data")
        val time = System.currentTimeMillis().toString()
        println("time = $time")

        //val sign = data?.let { SignV0.signatureParams("$data&$time") } ?: SignV0.signatureParams(time)
        //println("sign = $sign")
        return request.header("AppId", "C83BF4FBD9E14B6398F8AA00A7DAEDDC")
            .header("Key", "717658B599989C4B37E5D86A3BBD435F")
            .header("Time", time)
            //.header("Sign", sign)
    }


}