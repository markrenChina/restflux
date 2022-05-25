package com.zhipuchina.springcloud.proxyhttp

import java.util.UUID
import javax.print.attribute.standard.MediaSize.Other

data class TestPojo(
    val id: UUID = UUID.randomUUID(),
    val other: Int = 1
) {
}