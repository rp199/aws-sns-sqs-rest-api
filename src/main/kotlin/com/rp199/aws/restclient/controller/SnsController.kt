package com.rp199.aws.restclient.controller

import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SnsMessageClient
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/sns")
@Tag(name = "sns", description = "SNS Rest API")
class SnsController(val snsMessageClient: SnsMessageClient) {

    @Operation(summary = "Publishes a raw message to the given topic", tags = ["sns"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Message successfully published")])
    @PostMapping("/{queueName}/raw")
    fun publishRawMessage(@Parameter(description = "name of the topic where the message will be published")
                          @PathVariable("queueName") topicName: String
                          , @RequestBody(required = true) rawMessage: String) {
        snsMessageClient.sendMessage(topicName, rawMessage)
    }

    @Operation(summary = "Publishes a message to the given topic. Message attributes are optional", tags = ["sns"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Message successfully published")])
    @PostMapping("/{topicName}")
    fun publishMessage(@Parameter(description = "name of the topic where the message will be published")
                       @PathVariable("topicName") topicName: String,
                       @Parameter(description = "message request containing the payload and message attributes")
                       @RequestBody(required = true) publishMessageRequest: PublishMessageRequest) {
        snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes)
    }

    @Operation(summary = "Lists all the available topics names", tags = ["sns"])
    @ApiResponses(value = [ApiResponse(responseCode = "200", description = "Topic names returned")])
    @GetMapping("/list")
    fun listTopics(): List<String> {
        return snsMessageClient.listTopics()
    }
}