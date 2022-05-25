package com.zhipuchina.springcloud.proxyhttp

import com.fasterxml.jackson.databind.ObjectMapper
import com.zhipuchina.springcloud.proxyhttp.beans.MethodInfo
import com.zhipuchina.springcloud.proxyhttp.beans.ServerInfo
import com.zhipuchina.springcloud.proxyhttp.interfaces.AfterInterceptors
import com.zhipuchina.springcloud.proxyhttp.interfaces.BeforeInterceptors
import com.zhipuchina.springcloud.proxyhttp.interfaces.ProxyCreator
import com.zhipuchina.springcloud.proxyhttp.interfaces.RestHandler
import org.aopalliance.intercept.MethodInterceptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.http.HttpMethod
import org.springframework.util.ClassUtils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.reflect.*
import kotlin.reflect.KClass


/**
 *  使用jdk动态代理实现代理类
 *  todo 原型作用域 加入ioc容器
 */
class ProxyCreatorImpl(
    private val lbFunction: LoadBalancedExchangeFilterFunction?,
    private val objectMapper: ObjectMapper,
    private val beanFactory: BeanFactory
) : ProxyCreator {

    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    override fun <T> createProxy(type: Class<T>): T {
        logger.info("createProxy: $type")
        //根据接口得到api服务器信息
        val serverInfo = extractServerInfo(type)
        logger.info("serverInfo: $serverInfo")
        //给每一个代理类一个实现
        val handler: RestHandler = WebClientRestHandler(serverInfo,lbFunction,objectMapper)

        return Proxy.newProxyInstance(type.classLoader, arrayOf(type),
            object : InvocationHandler {
                override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any {
                    // If the method is a method from Object then defer to normal invocation.
                    if (method.declaringClass == Any::class.java) {
                        return method.invoke(this, args)
                    }
                    //根据方法和参数得到调用信息
                    val methodInfo = extractMethodInfo(method, args)
                    logger.info("methodInfo: $methodInfo")
                    //调用rest
                    return handler.invokeRest(methodInfo)
                }

                /**
                 * 根据方法定义的调用参数得到调用的相关信息
                 */
                private fun extractMethodInfo(method: Method, args: Array<out Any>?): MethodInfo {
                    val methodInfo = MethodInfo()
                    extractUriAndMethod(method, methodInfo)
                    extractReturnInfo(method, methodInfo)
                    if (args == null) return methodInfo
                    extractRequestParamAndBody(method, args, methodInfo)
                    return methodInfo
                }

                private fun extractReturnInfo(method: Method, methodInfo: MethodInfo) {
                    // 返回flux还是mono
                    methodInfo.isReturnFlux = method.returnType.isAssignableFrom(Flux::class.java)
                    //logger.info("isFlux ${methodInfo.isReturnFlux}")
                    // 得到返回对象的实际类型
                    methodInfo.returnElementType = extractElementType2(methodInfo, method.genericReturnType)
                }

                /**
                 * 得到泛型类型的实际类型
                 */
                private fun extractElementType(genericReturnType: Type): Class<*> {
                    return (genericReturnType as Class<*>)
                }

                /**
                 * 得到泛型类型的实际类型2
                 */
                private fun extractElementType2(methodInfo: MethodInfo, genericReturnType: Type)
                        : Type {
                    val actualTypeArguments = (genericReturnType as ParameterizedType).actualTypeArguments
                    return actualTypeArguments[0]
                }

                private fun extractRequestParamAndBody(
                    method: Method,
                    args: Array<out Any>,
                    methodInfo: MethodInfo
                ) {
                    //得到调用的参数和body
                    val parameters = method.parameters
                    val params = LinkedMultiValueMap<String, String>()
                    for (index in parameters.indices) {
                        //是否带@PathVariable
                        val aPath = parameters[index].getAnnotation(PathVariable::class.java)
                        aPath?.let {
                            val pathName = if (it.value == "") parameters[index].name else it.value
                            methodInfo.uri = methodInfo.uri.replace("{$pathName}", args[index].toString())
                        }
                        //是否带@RequestParam
                        val aParam = parameters[index].getAnnotation(RequestParam::class.java)
                        aParam?.let {
                            val paramsName = if (it.value == "") parameters[index].name else it.value
                            params[paramsName] = args[index].toString()
                        }
                        //是否带了requestBody
                        val aBody = parameters[index].getAnnotation(RequestBody::class.java)
                        aBody?.let {
                            methodInfo.body = args[index] as? Mono<*> ?: Mono.just(args[index])
                            //请求对象的实际类型
                            methodInfo.bodyElementType = extractElementType(parameters[index].parameterizedType)
                        }
                    }
                    methodInfo.params = params
                }

                private fun extractUriAndMethod(
                    method: Method,
                    methodInfo: MethodInfo
                ) {
                    val annotations = method.annotations
                    for (e in annotations) {
                        when (e) {
                            is GetMapping -> {
                                methodInfo.uri = if (e.value.isEmpty()) "/" else e.value[0]
                                methodInfo.method = HttpMethod.GET
                                methodInfo.returnContextType = e.consumes
                                methodInfo.sendContentType = e.produces
                            }
                            is PostMapping -> {
                                methodInfo.uri = if (e.value.isEmpty()) "/" else e.value[0]
                                methodInfo.method = HttpMethod.POST
                                methodInfo.returnContextType = e.consumes
                                methodInfo.sendContentType = e.produces
                            }
                            is DeleteMapping -> {
                                methodInfo.uri = if (e.value.isEmpty()) "/" else e.value[0]
                                methodInfo.method = HttpMethod.DELETE
                                methodInfo.returnContextType = e.consumes
                                methodInfo.sendContentType = e.produces
                            }
                            is PutMapping -> {
                                methodInfo.uri = if (e.value.isEmpty()) "/" else e.value[0]
                                methodInfo.method = HttpMethod.PUT
                                methodInfo.returnContextType = e.consumes
                                methodInfo.sendContentType = e.produces
                            }
                        }
                    }
                }
            }) as T
    }

    /**
     * 提取服务器信息
     * 返回一个对象而不是直接返回url。是为了以后扩展
     */
    private fun extractServerInfo(type: Class<*>): ServerInfo {
        val annotation = type.getAnnotation(ApiServer::class.java)
        val attributes = AnnotationUtils.getAnnotationAttributes(annotation)
        //验证拦截器
        val beforeInterceptorsUnits: ArrayList<BeforeInterceptors> = ArrayList()
        val beforeInterceptors = attributes["beforeInterceptor"]
        if (beforeInterceptors is Array<*> && beforeInterceptors.isNotEmpty()) {
            beforeInterceptors.forEach { interceptor ->
                val kClass = interceptor as? Class<*>
                if (kClass != null && BeforeInterceptors::class.java.isAssignableFrom(kClass)) {
                    //beforeInterceptorsUnits.add(kClass.newInstance() as BeforeInterceptors)
                    beforeInterceptorsUnits.add(beanFactory.getBean(kClass) as BeforeInterceptors)
                }
            }
        }
        val afterInterceptorsUnits: ArrayList<AfterInterceptors> = ArrayList()
        val afterInterceptors = attributes["afterInterceptor"]
        if (afterInterceptors is Array<*> && afterInterceptors.isNotEmpty()) {
            afterInterceptors.forEach { interceptor ->
                val kClass = interceptor as? Class<*>
                if (kClass != null && AfterInterceptors::class.java.isAssignableFrom(kClass)) {
                    //afterInterceptorsUnits.add(kClass.newInstance() as AfterInterceptors)
                    afterInterceptorsUnits.add(beanFactory.getBean(kClass) as AfterInterceptors)
                }
            }
        }

        return ServerInfo(
            type.getAnnotation(ApiServer::class.java).value,
            type.getAnnotation(ApiServer::class.java).isLoadBalanced,
            if (beforeInterceptorsUnits.isNotEmpty()) beforeInterceptorsUnits.toArray(arrayOf(beforeInterceptorsUnits[0])) else arrayOf(),
            if (afterInterceptorsUnits.isNotEmpty()) afterInterceptorsUnits.toArray(arrayOf(afterInterceptorsUnits[0])) else arrayOf()
        )
    }


}