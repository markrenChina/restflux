package com.zhipuchina.springcloud.proxyhttp

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.zhipuchina.springcloud.proxyhttp.beans.MethodInfo
import com.zhipuchina.springcloud.proxyhttp.beans.ServerInfo
import com.zhipuchina.springcloud.proxyhttp.interfaces.RestHandler
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.Type

class WebClientRestHandler(
    override val serverInfo: ServerInfo,
    lbFunction: LoadBalancedExchangeFilterFunction?,
    private val objectMapper: ObjectMapper
) : RestHandler {

    private val webClient = WebClient.create()
    private val loadBalancedClient = lbFunction?.let { WebClient.builder().filter(it).build() } ?: webClient

    /**
     * 处理rest请求
     */
    override fun invokeRest(methodInfo: MethodInfo): Any {
        val client = if (serverInfo.isLoadBalanced) {
            if (loadBalancedClient == webClient) {
                throw RuntimeException("没有注入负载均衡")
            }
            loadBalancedClient
        } else webClient
        // BaseCodecConfigurer 增加自定义
        //增加处理策略
        val strategy = ExchangeStrategies.builder().codecs {
        }.build()
        WebClient.builder().exchangeStrategies(strategy)
        //val client = WebClient.create()
        //构建返回类型
        val backContentType: Array<MediaType> = getMediaTypes(methodInfo.returnContextType)

        //构建发送类型
        val sendContentType: Array<MediaType> = getMediaTypes(methodInfo.sendContentType)

        //构建Uri
        val uri = UriComponentsBuilder
            .fromUriString(serverInfo.url)
            .path(methodInfo.uri)
            .queryParams(methodInfo.params).build().encode().toUri()

        val request = client
            .method(methodInfo.method)
            .uri(uri)
            .contentType(sendContentType[0])
            /*.headers {
                //底层的数据结构 MultiValueMap<String, String>

            }*/
            .accept(*backContentType)
        //发出请求
        var fullRequest = methodInfo.body?.let {
            request.body(it, methodInfo.bodyElementType!!)
        } ?: request


        //添加前置拦截器
        if (serverInfo.beforeInterceptor.isNotEmpty()) {
            serverInfo.beforeInterceptor.forEach {
                fullRequest = it.invoke(fullRequest, methodInfo.body,methodInfo.bodyElementType)
            }
        }
        var retrieve = fullRequest.retrieve()

        //处理异常
        retrieve.onStatus({ status -> status.value() == 404 }, { Mono.just(RuntimeException("not found")) })

        //添加后置拦截器
        if (serverInfo.afterInterceptor.isNotEmpty()) {
            serverInfo.afterInterceptor.forEach {
                retrieve = it.invoke(retrieve)
            }
        }

        //处理body
        return if (MediaType.APPLICATION_JSON in backContentType) {
            // 先转String 是为了应对 text/html 却传回json字符串的请求
            if (methodInfo.isReturnFlux) {
                retrieve.bodyToMono(String::class.java).map {
                    transitionList(methodInfo, it)
                }.flatMapMany { Flux.fromIterable(it as MutableIterable<*>) }
            } else {
                retrieve.bodyToMono(String::class.java).map {
                    //println(it)
                    transition(methodInfo, it)
                }
            }
        } else {
            if (methodInfo.isReturnFlux) {
                retrieve.bodyToFlux(String::class.java)
            } else {
                retrieve.bodyToMono(String::class.java)
            }
        }
    }

    private fun getMediaTypes(contextType: Array<String>) =
        if (contextType.isEmpty()) {
            arrayOf(MediaType.APPLICATION_JSON)
        } else {
            val array: Array<MediaType> = Array(contextType.size) { MediaType.APPLICATION_JSON }
            for (index in contextType.indices) {
                array[index] = MediaType(
                    contextType[index].split("/")[0],
                    contextType[index].split("/")[1].split(";")[0]
                )
            }
            array
        }

    private fun transition(
        methodInfo: MethodInfo,
        json: String,
    ): Any {
        return objectMapper.readValue(json, getJavaType(methodInfo.returnElementType))
    }

    private fun transitionList(
        methodInfo: MethodInfo,
        json: String,
    ): Any {
        return objectMapper.readValue(json, getJavaType(List::class.java, methodInfo.returnElementType))
        //objectMapper.readValue(json, getJavaType(methodInfo.returnElementType))
    }

    private fun getJavaType(type: Type): JavaType = objectMapper.typeFactory.constructType(type)

    private fun getJavaType(parentClazz: Class<*>, type: Type): JavaType =
        objectMapper.typeFactory.constructParametricType(parentClazz, getJavaType(type))


/*if (methodInfo.returnCommonType.contains(ResultV1::class.java.typeName)) {
        JacksonUtils.fromResultV1Json(json, methodInfo.returnElementType)
    } else {
        JacksonUtils.fromRespJson(json, methodInfo.returnElementType)
    }*/

}
