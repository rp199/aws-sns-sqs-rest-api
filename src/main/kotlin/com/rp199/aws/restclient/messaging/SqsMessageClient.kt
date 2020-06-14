package com.rp199.aws.restclient.messaging

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.PurgeQueueRequest
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class SqsMessageClient(private val amazonSQSAsync: AmazonSQSAsync) {
    private val queueMessagingTemplate = QueueMessagingTemplate(amazonSQSAsync)

    fun sendMessage(queueName: String, message: String, messageAttributeMap: Map<String, String>? = emptyMap()) {
        return queueMessagingTemplate.convertAndSend(queueName, message, messageAttributeMap)
    }

    fun receiveMessage(queueName: String): String? {
        return queueMessagingTemplate.receiveAndConvert(queueName, String::class.java)
    }

    fun purgeQueue(queueName: String) {
        amazonSQSAsync.purgeQueue(PurgeQueueRequest(amazonSQSAsync.getQueueUrl(queueName).queueUrl))
    }

    fun listQueues(): List<String> {
        return amazonSQSAsync.listQueues().queueUrls
    }
}