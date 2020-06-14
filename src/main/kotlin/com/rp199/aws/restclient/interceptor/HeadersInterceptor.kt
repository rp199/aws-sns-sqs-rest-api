package com.rp199.aws.restclient.interceptor

import com.rp199.aws.restclient.store.ClientType
import com.rp199.aws.restclient.store.ClientTypeStore
import org.slf4j.LoggerFactory
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val CLIENT_TYPE_HEADER = "clientType"

@Component
class HeadersInterceptor(val defaultClientType: ClientType) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(HandlerInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val clientType = request.getHeader(CLIENT_TYPE_HEADER)?.let {
            ClientType.fromString(it)
        } ?: defaultClientType

        ClientTypeStore.setClientType(clientType)
        logger.info("""Using client mode: ${ClientTypeStore.getClientType()}""")
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, @Nullable e: Exception?) {
        ClientTypeStore.clear()
    }
}