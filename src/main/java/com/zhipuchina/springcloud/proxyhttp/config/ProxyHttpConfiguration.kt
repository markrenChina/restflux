package com.zhipuchina.springcloud.proxyhttp.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.zhipuchina.springcloud.proxyhttp.ProxyCreatorImpl
import com.zhipuchina.springcloud.proxyhttp.interfaces.ProxyCreator
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
//@AutoConfigureAfter(JacksonAutoConfiguration::class, ReactorLoadBalancerClientAutoConfiguration::class)
class ProxyHttpConfiguration {

    //test 传建jdk代理工具类
    @Bean
    //@ConditionalOnClass(ReactorLoadBalancerExchangeFilterFunction::class)
    //@ConditionalOnMissingBean(ProxyCreator::class)
    fun proxyCreator(
        @Autowired objectMapper: ObjectMapper,
        @Autowired(required = false) lbFunction: LoadBalancedExchangeFilterFunction? = null,
        @Autowired beanFactory: BeanFactory
    ): ProxyCreator {
        //val bak = beanFactory.getBean(DebugBeforeInterceptor)
        return ProxyCreatorImpl(lbFunction,objectMapper,beanFactory)
    }
}

