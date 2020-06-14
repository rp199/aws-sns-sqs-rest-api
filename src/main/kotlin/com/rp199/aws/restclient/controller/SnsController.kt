package com.rp199.aws.restclient.controller

import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SnsMessageClient
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/sns")
class SnsController(val snsMessageClient: SnsMessageClient) {

    @PostMapping("/{queueName}/raw")
    fun publishRawMessage(@PathVariable("queueName") queueName: String
                          , @RequestBody(required = true) rawMessage: String) {
        snsMessageClient.sendMessage(queueName, rawMessage)
    }

    @PostMapping("/{topicName}")
    fun publishMessage(@PathVariable("topicName") topicName: String,
                       @RequestBody(required = true) publishMessageRequest: PublishMessageRequest) {
        snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes)
    }

    @GetMapping("/list")
    fun listTopics(): List<String> {
        return snsMessageClient.listTopics()
    }
}