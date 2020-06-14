package com.rp199.aws.restclient.controller

import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SqsMessageClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/sqs")
class SqsController(private val sqsMessageClient: SqsMessageClient) {

    @PostMapping("/{queueName}/raw")
    fun publishRawMessage(@PathVariable("queueName") queueName: String
                          , @RequestBody(required = true) rawMessage: String) {
        sqsMessageClient.sendMessage(queueName, rawMessage)
    }

    @PostMapping("/{queueName}")
    fun publishMessage(@PathVariable("queueName") queueName: String,
                       @RequestBody(required = true) publishMessageRequest: PublishMessageRequest) {
        sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes)
    }

    @GetMapping(value = ["/{queueName}"])
    fun receiveMessage(@PathVariable("queueName") queueName: String): ResponseEntity<String> {
        return ResponseEntity.of(Optional.ofNullable(sqsMessageClient.receiveMessage(queueName)))
    }

    @DeleteMapping("/{queueName}")
    fun purgeQueue(@PathVariable("queueName") queueName: String) {
        sqsMessageClient.purgeQueue(queueName)

    }

    @GetMapping("/list")
    fun listQueues(): List<String> {
        return sqsMessageClient.listQueues()

    }
}