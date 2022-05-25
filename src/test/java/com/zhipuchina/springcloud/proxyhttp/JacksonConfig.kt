package com.zhipuchina.springcloud.proxyhttp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import org.springframework.boot.jackson.JsonComponent
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset

@JsonComponent
//@Configuration
class JacksonConfig {

    /**
     * 覆盖默认jaskson的格式化
     */
    /*@Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder
                //.serializerByType(Long.TYPE, ToStringSerializer.instance)
                //.serializerByType(Long::class.java, ToStringSerializer.instance)
                //.serializerByType(Date::class.java, DateSerializer())
                //.serializerByType(LocalDate.class, new LocalDateSerializer())
                .serializerByType(String::class.java, StringSerializer)
                .serializerByType(LocalDateTime::class.java, LocalDateTimeSerializer)
                .deserializerByType(String::class.java, StringDeserializer)
                //.deserializerByType(Date.class, new DateDeserializer())
                //.deserializerByType(LocalDate.class, new LocalDateDeserializer())
                .deserializerByType(LocalDateTime::class.java, LocalDateTimeDeserializer)
                .failOnEmptyBeans(false)
                .failOnUnknownProperties(false);
        }
    }*/

    internal class LocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {

        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext?): LocalDateTime? =
            LocalDateTime.ofEpochSecond(jsonParser.valueAsLong, 0, ZoneOffset.ofHours(8))
    }

    internal class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {

        @Throws(IOException::class)
        override fun serialize(value: LocalDateTime?, gen: JsonGenerator, serializers: SerializerProvider) {
            value?.let { gen.writeNumber(it.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()) }
        }
    }

    /**
     * jackson String 反序列化拦截器
     * String 解析时如是空字符串“”则返回null
     * @author 任家立
     */
    internal class StringDeserializer : JsonDeserializer<String>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): String? {
            return p.text.ifEmpty { null }
        }
    }

    internal class StringSerializer : JsonSerializer<String>() {

        override fun serialize(value: String?, gen: JsonGenerator, serializers: SerializerProvider?) {
            value?.let {
                if (it.isEmpty()) {
                    gen.writeNull()
                } else gen.writeString(it)
            } ?: gen.writeNull()
        }
    }
}