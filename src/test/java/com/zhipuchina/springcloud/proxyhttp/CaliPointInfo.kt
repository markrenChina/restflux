package com.zhipuchina.springcloud.proxyhttp

import java.io.Serializable

/**
 * 定标点信息
 *
 * @param CdDr 镉测量值
 * @param CdConc 镉浓度值(mg/kg)
 * @param PbDr 铅测量值
 * @param PbConc 铅浓度值(mg/kg)
 */
data class CaliPointInfo(
    var No: Int?=null,
    var CdDr: Float?=null,
    var CdConc: Float?=null,
    var PbDr: Float?=null,
    var PbConc: Float?=null
) : Serializable {

    override fun toString(): String {
        return """{
            |"CdDr":$CdDr
            |"CdConc":$CdConc
            |"PbDr:$PbDr
            |"PbConc":$PbConc
            |}""".trimMargin()
    }

}
