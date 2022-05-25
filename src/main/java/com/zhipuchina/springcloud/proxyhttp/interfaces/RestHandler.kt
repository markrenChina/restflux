package com.zhipuchina.springcloud.proxyhttp.interfaces

import com.zhipuchina.springcloud.proxyhttp.beans.MethodInfo
import com.zhipuchina.springcloud.proxyhttp.beans.ServerInfo


/**
 * rest请求调用handler
 * @author 任家立
 * @since 1.0.1
 */
interface RestHandler {
    /**
     * 初始化服务器信息
     */
    val serverInfo: ServerInfo

    /**
     * 调用rest请求，返回接口
     */
    fun invokeRest(methodInfo: MethodInfo): Any
}