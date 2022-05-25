package com.zhipuchina.springcloud.proxyhttp

import com.zhipuchina.springcloud.proxyhttp.interfaces.ProxyCreator
import com.zhipuchina.springcloud.proxyhttp.utils.creatServerApi
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpConfig {

    @Bean
    fun testHttp(@Autowired proxyCreator: ProxyCreator): FactoryBean<HeavyMetalNewApi?> =
        creatServerApi(proxyCreator, HeavyMetalNewApi::class.java)
}