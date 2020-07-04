package com.rp199.aws.restclient.controller

import com.beust.klaxon.Klaxon
import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SqsMessageClient
import com.rp199.aws.restclient.store.ClientType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.kotest.spring.SpringListener
import io.mockk.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WebMvcTest(SqsController::class)
internal class SqsControllerTest : BehaviorSpec() {

    override fun listeners() = listOf(SpringListener)

    @TestConfiguration
    class SqsControllerTestConfiguration {
        @Bean
        fun sqsMessageClient() = mockk<SqsMessageClient>(relaxed = true)

        @Bean
        fun clientType() = mockk<ClientType>(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var sqsMessageClient: SqsMessageClient

    private val queueName = "my-queue"

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        if (testCase.isTopLevel()) {
            clearAllMocks()
        }
    }

    init {
        Given("Spring context loads") {}

        Given("That there are queues") {
            val expectedTopicListResponse = listOf("my-queue1", "my-queue2", "my-queue3")
            every { sqsMessageClient.listQueues() } returns expectedTopicListResponse

            When("I Make GET request to /sqs/list endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/list"))
                Then("I get a 200 - OK response and the SQS client publishes the message") {
                    val response = request.andExpect(MockMvcResultMatchers.status().isOk).andReturn().response
                    response.contentAsString shouldBe Klaxon().toJsonString(expectedTopicListResponse).replace(" ", "")
                    verify { sqsMessageClient.listQueues() }
                }
            }
        }

        Given("That there are no queues") {
            every { sqsMessageClient.listQueues() } returns emptyList()
            When("I Make GET request to /sqs/list endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/list"))
                Then("I get a 200 - OK response and the SQS client publishes the message") {
                    val response = request.andExpect(MockMvcResultMatchers.status().isOk).andReturn().response
                    response.contentAsString shouldBe Klaxon().toJsonString(emptyList<String>())
                    verify { sqsMessageClient.listQueues() }
                }
            }
        }

        Given("A message with message attributes") {
            val publishMessageRequest = PublishMessageRequest("Hello there!",
                    mapOf("someMessageAttribute" to "666", "anotherMessageAttribute" to "someValue"))
            When("I Make POST request to /sqs/${queueName} endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Klaxon().toJsonString(publishMessageRequest)))
                Then("I get a 200 - OK response and the SQS client publishes the message") {
                    request.andExpect(MockMvcResultMatchers.status().isOk)
                    verify { sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
                }
            }
        }

        Given("A message without message attributes") {
            val publishMessageRequest = PublishMessageRequest("Hello there!", null)
            When("I Make POST request to /sqs/${queueName} endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Klaxon().toJsonString(publishMessageRequest)))
                Then("I get a 200 - OK response and the SQS client publishes the message") {
                    request.andExpect(MockMvcResultMatchers.status().isOk)
                    verify { sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
                }
            }
        }

        Given("An empty request body ") {
            When("I Make POST request to /sqs/${queueName} endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/${queueName}"))
                        .andDo(MockMvcResultHandlers.print())
                Then("I get a 400 - bad request response (400) and the SQS client is not called") {
                    request.andExpect(MockMvcResultMatchers.status().isBadRequest)
                    verify { sqsMessageClient wasNot called }
                }
            }
        }

        Given("A non JSON message") {
            val message = "I'm not JSON"
            When("I Make POST request to /sqs/${queueName} endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/${queueName}")
                        .content(message))
                Then("I get a 415 - unsupported media type response and the SQS client is not called") {
                    request.andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType)
                    verify { sqsMessageClient wasNot called }
                }
            }
        }

        Given("A JSON message with a unexpected format") {
            val message = """
            {
                "message": "This message has a unexpected format"
            }
            """.trimIndent()

            When("I Make POST request to /sqs/${queueName} endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/${queueName}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message))
                Then("I get a 400 - bad request response and the SQS client is not called") {
                    request.andExpect(MockMvcResultMatchers.status().isBadRequest)
                    verify { sqsMessageClient wasNot called }
                }
            }
        }

        Given("A simple message") {
            val message = "Hello there!"
            When("I Make POST request to /sqs/$queueName/raw endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName/raw")
                        .content(message))
                Then("I get a 200 - OK response and the SQS client publishes the message") {
                    request.andExpect(MockMvcResultMatchers.status().isOk)
                    verify { sqsMessageClient.sendMessage(queueName, message) }
                }
            }
        }

        Given("An empty request body") {
            When("I Make POST request to /sqs/$queueName/raw endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName/raw"))
                        .andDo(MockMvcResultHandlers.print())
                Then("I get a 400 - bad request response (400) and the SQS client is not called") {
                    request.andExpect(MockMvcResultMatchers.status().isBadRequest)
                    verify { sqsMessageClient wasNot called }
                }
            }
        }

        Given("Queue has a message") {
            val message = "Hello there!"
            every { sqsMessageClient.receiveMessage(queueName) } returns message
            When("I Make GET request to /sqs/$queueName endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/$queueName"))
                Then("I get the message") {
                    val response = request.andExpect(MockMvcResultMatchers.status().isOk).andReturn()
                    response.response.contentAsString shouldBe message
                    verify { sqsMessageClient.receiveMessage(queueName) }
                }
            }
        }

        Given("Queue is empty") {
            every { sqsMessageClient.receiveMessage(queueName) } returns null
            When("I Make GET request to /sqs/$queueName endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/$queueName"))
                Then("I get a 404 - Not Found response (400)") {
                    request.andExpect(MockMvcResultMatchers.status().isNotFound)
                    verify { sqsMessageClient.receiveMessage(queueName) }
                }
            }
        }

        Given("Queue with messages") {
            When("I Make DELETE request to /sqs/$queueName endpoint") {
                val request = mockMvc.perform(MockMvcRequestBuilders.delete("/sqs/$queueName"))
                Then("I get a 200 and the queue is purged") {
                    request.andExpect(MockMvcResultMatchers.status().isOk)
                    verify { sqsMessageClient.purgeQueue(queueName) }
                }
            }
        }

    }
}