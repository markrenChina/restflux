package com.zhipuchina.springcloud.proxyhttp

import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@ApiServer(
    "http://127.0.0.1:20103/g1/h/hmn",
    isLoadBalanced = false,
    beforeInterceptor = [DebugBeforeInterceptor::class],
    afterInterceptor = [DebugAfterInterceptor::class]
)
interface HeavyMetalNewApi {

    @GetMapping("/paramSetting2/{batchNum}")
    fun getBatchNumList(@PathVariable(value = "batchNum") batchNum: String): Mono<ResultV1<List<ParamSetting2>>>

    @GetMapping("/paramSetting2/")
    fun patch(): Flux<BatchNumNewest>

    @PostMapping("/paramSetting2")
    fun updateParam(@RequestBody paramSetting2: TestPojo): Mono<TestPojo>
}