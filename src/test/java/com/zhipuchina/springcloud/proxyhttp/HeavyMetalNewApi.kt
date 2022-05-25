package com.zhipuchina.springcloud.proxyhttp

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ApiServer(
    "http://192.168.1.255:8080",
    isLoadBalanced = false,
    beforeInterceptor = [DebugBeforeInterceptor::class],
    afterInterceptor = [DebugAfterInterceptor::class]
)
interface HeavyMetalNewApi {

    @GetMapping("/paramSetting2/")
    fun patch(): Flux<BatchNumNewest>

    @PostMapping("/paramSetting2")
    fun updateParam(@RequestBody paramSetting2: TestPojo): Mono<TestPojo>
}