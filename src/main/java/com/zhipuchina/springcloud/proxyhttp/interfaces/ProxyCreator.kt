package com.zhipuchina.springcloud.proxyhttp.interfaces

/**
 * 创建代理类接口
 * @author 任家立
 * @since 1.0.0
 */
interface ProxyCreator {
    fun <T> createProxy(type: Class<T>): T
}
