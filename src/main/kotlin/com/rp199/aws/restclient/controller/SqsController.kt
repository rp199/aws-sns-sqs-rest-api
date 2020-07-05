package com.rp199.aws.restclient.controller

import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SqsMessageClient
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/sqs")
@Tag(name = "sqs", description = "SQS Rest API")
class SqsController(private val sqsMessageClient: SqsMessageClient) {

    @Operation(summary = "Publishes a raw message to the given queue", tags = ["sqs"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Message successfully published")])
    @PostMapping("/{queueName}/raw")
    fun publishRawMessage(@Parameter(description = "name of the queue where the message will be published")
                          @PathVariable("queueName") queueName: String
                          , @RequestBody(required = true) rawMessage: String) {
        sqsMessageClient.sendMessage(queueName, rawMessage)
    }

    @Operation(summary = "Publishes a message to the given queue. Message attributes are optional", tags = ["sqs"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Message successfully published")])
    @PostMapping("/{queueName}")
    fun publishMessage(@Parameter(description = "name of the queue where the message will be published")
                       @PathVariable("queueName") queueName: String,
                       @RequestBody(required = true) publishMessageRequest: PublishMessageRequest) {
        sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes)
    }

    @Operation(summary = "Polls the latest message from the given queue", tags = ["sqs"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Message successfully returned"),
        ApiResponse(responseCode = "404", description = "No message found in the queue")
    ])
    @GetMapping(value = ["/{queueName}"])
    fun receiveMessage(@Parameter(description = "name of the queue to be polled")
                       @PathVariable("queueName") queueName: String): ResponseEntity<String> {
        return ResponseEntity.of(Optional.ofNullable(sqsMessageClient.receiveMessage(queueName)))
    }

    @Operation(summary = "Purge the given queue", tags = ["sqs"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Queue was purged")])
    @DeleteMapping("/{queueName}")
    fun purgeQueue(@Parameter(description = "name of the queue to be purged")
                   @PathVariable("queueName") queueName: String) {
        sqsMessageClient.purgeQueue(queueName)

    }

    @Operation(summary = "Lists all the available queues names", tags = ["sqs"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Queue names returned")])
    @GetMapping("/list")
    fun listQueues(): List<String> {
        return sqsMessageClient.listQueues()

    }
}