package com.rp199.aws.restclient.messaging

import com.amazonaws.services.sns.AmazonSNS
import org.springframework.cloud.aws.messaging.core.NotificationMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope

@Component
@RequestScope
class SnsMessageClient(private val amazonSNS: AmazonSNS) {
    protected val notificationTemplate = NotificationMessagingTemplate(amazonSNS)

    fun sendMessage(topicName: String, message: String, messageAttributeMap: Map<String, String>? = emptyMap()) {
        notificationTemplate.convertAndSend(topicName, message, messageAttributeMap)
    }

    fun listTopics(): List<String> {
        return amazonSNS.listTopics().topics.map { it.topicArn }
    }
}