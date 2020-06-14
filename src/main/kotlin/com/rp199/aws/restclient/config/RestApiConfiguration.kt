package com.rp199.aws.restclient.config

import com.rp199.aws.restclient.interceptor.HeadersInterceptor
import com.rp199.aws.restclient.interceptor.LoggingInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
class RestApiConfiguration(private val loggingInterceptor: LoggingInterceptor, private val headersInterceptor: HeadersInterceptor) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        super.addInterceptors(registry)
        registry.addInterceptor(loggingInterceptor)
        registry.addInterceptor(headersInterceptor)
    }

}