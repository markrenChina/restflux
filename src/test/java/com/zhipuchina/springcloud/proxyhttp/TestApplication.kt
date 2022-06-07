package com.zhipuchina.springcloud.proxyhttp

import com.zhipuchina.springcloud.proxyhttp.annotation.EnableReactiveHttpClient
import com.zhipuchina.springcloud.proxyhttp.interfaces.BeforeInterceptors
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@SpringBootApplication
@EnableReactiveHttpClient
class TestApplication {
}

fun main(args: Array<String>) {
    val applicationContext: ConfigurableApplicationContext = runApplication<TestApplication>(*args)
    val api = applicationContext.getBean(HeavyMetalNewApi::class.java)
    api.patch().collectList().block()?.stream()?.forEach(System.out::println)
    //api.updateParam(TestPojo()).block()?.let { println(it) }
    println()
}