package com.zhipuchina.springcloud.proxyhttp

import com.zhipuchina.springcloud.proxyhttp.interfaces.AfterInterceptors
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DebugAfterInterceptor :AfterInterceptors{
    override fun <S : WebClient.ResponseSpec> invoke(response: S): S {
        response.toString()
        return response
    }
}