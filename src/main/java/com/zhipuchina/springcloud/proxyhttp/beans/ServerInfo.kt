package com.zhipuchina.springcloud.proxyhttp.beans

import com.zhipuchina.springcloud.proxyhttp.interfaces.AfterInterceptors
import com.zhipuchina.springcloud.proxyhttp.interfaces.BeforeInterceptors
import org.aopalliance.intercept.MethodInterceptor

/**
 *  服务器信息类
 *  这个类对应ApiServer注解
 *  如果ApiServer注解属性扩展这个类也应该相应扩展
 */

data class ServerInfo(
    val url: String,
    val isLoadBalanced: Boolean = false,
    var beforeInterceptor: Array<BeforeInterceptors> = arrayOf(),
    var afterInterceptor: Array<AfterInterceptors> = arrayOf()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerInfo

        if (url != other.url) return false
        if (isLoadBalanced != other.isLoadBalanced) return false
        if (!beforeInterceptor.contentEquals(other.beforeInterceptor)) return false
        if (!afterInterceptor.contentEquals(other.afterInterceptor)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = url.hashCode()
        result = 31 * result + isLoadBalanced.hashCode()
        result = 31 * result + beforeInterceptor.contentHashCode()
        result = 31 * result + afterInterceptor.contentHashCode()
        return result
    }
}
