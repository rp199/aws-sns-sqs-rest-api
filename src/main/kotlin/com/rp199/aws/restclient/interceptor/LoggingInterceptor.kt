package com.rp199.aws.restclient.interceptor

import org.slf4j.LoggerFactory
import org.springframework.lang.Nullable
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Component
class LoggingInterceptor : HandlerInterceptor {

    private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val requestId = UUID.randomUUID()
        logger.info("""Starting requestId: $requestId, HttpMethod: ${request.method}, URI: ${request.requestURI}""")
        request.setAttribute("startTime", System.currentTimeMillis())
        request.setAttribute("requestId", requestId)
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, @Nullable e: Exception?) {
        super.afterCompletion(request, response, handler, e)
        val startTime = request.getAttribute("startTime") as Long
        val endTime = System.currentTimeMillis()
        val executeTime = endTime - startTime
        logger.info("Completed requestId ${request.getAttribute("requestId")}, Handle: $handler, request took: $executeTime")
        request.removeAttribute("startTime")
    }
}