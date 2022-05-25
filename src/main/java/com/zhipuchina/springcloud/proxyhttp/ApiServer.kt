package com.zhipuchina.springcloud.proxyhttp

import org.aopalliance.intercept.MethodInterceptor
import kotlin.reflect.KClass

/**
 * api接口标注
 * @author 任家立
 * v1.0.1 修改默认打开负载均衡
 * @since v1.0.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiServer(
    val value: String = "",
    val isLoadBalanced: Boolean = true,
    /**
     * must be extended MethodInterceptor
     * default Unit::class
     * @see MethodInterceptor
     */
    val beforeInterceptor: Array<KClass<*>> = [],
    /**
     * must be extended MethodInterceptor
     * @see MethodInterceptor
     */
    val afterInterceptor: Array<KClass<*>> = []
)