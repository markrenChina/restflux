package com.zhipuchina.springcloud.proxyhttp

import java.time.LocalDateTime

data class BatchNumNewest(
    val batchNum: String = "",
    val updateTime: LocalDateTime = LocalDateTime.MIN
)