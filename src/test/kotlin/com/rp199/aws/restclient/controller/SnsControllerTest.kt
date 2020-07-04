package com.rp199.aws.restclient.controller

import com.beust.klaxon.Klaxon
import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SnsMessageClient
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SnsController::class)
internal class SnsControllerTest : BehaviorSpec() {
    override fun listeners() = listOf(SpringListener)

    @TestConfiguration
    class SnsControllerTestConfiguration {
        @Bean
        fun snsMessageClient() = mockk<SnsMessageClient>(relaxed = true)

        @Bean
        fun clientType() = ClientType.LOCAL
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var snsMessageClient: SnsMessageClient

    private val topicName = "my-topic"

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)
        if (testCase.isTopLevel()) {
            clearAllMocks()
        }
    }

    init {
        Given("Spring context loads") {}
        Given("That there are topics") {
            val expectedTopicListResponse = listOf("my-topic1", "my-topic2", "my-topic3")
            every { snsMessageClient.listTopics() } returns expectedTopicListResponse

            When("I Make GET request to /sns/list endpoint") {
                val request = mockMvc.perform(get("/sns/list"))
                Then("I get a 200 - OK response and the SNS client publishes the message") {
                    val response = request.andExpect(status().isOk).andReturn().response
                    response.contentAsString shouldBe Klaxon().toJsonString(expectedTopicListResponse).replace(" ", "")
                    verify { snsMessageClient.listTopics() }
                }
            }
        }

        Given("That there are no topics") {
            every { snsMessageClient.listTopics() } returns emptyList()
            When("I Make GET request to /sns/list endpoint") {
                val request = mockMvc.perform(get("/sns/list"))
                Then("I get a 200 - OK response and the SNS client publishes the message") {
                    val response = request.andExpect(status().isOk).andReturn().response
                    response.contentAsString shouldBe Klaxon().toJsonString(emptyList<String>())
                    verify { snsMessageClient.listTopics() }
                }
            }
        }

        Given("A message with message attributes") {
            val publishMessageRequest = PublishMessageRequest("Hello there!",
                    mapOf("someMessageAttribute" to "666", "anotherMessageAttribute" to "someValue"))
            When("I Make POST request to /sns/{$topicName} endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Klaxon().toJsonString(publishMessageRequest)))
                Then("I get a 200 - OK response and the SNS client publishes the message") {
                    request.andExpect(status().isOk)
                    verify { snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
                }
            }
        }

        Given("A message without message attributes") {
            val publishMessageRequest = PublishMessageRequest("Hello there!", null)
            When("I Make POST request to /sns/{$topicName} endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(Klaxon().toJsonString(publishMessageRequest)))
                Then("I get a 200 - OK response and the SNS client publishes the message") {
                    request.andExpect(status().isOk)
                    verify { snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
                }
            }
        }

        Given("An empty request body ") {
            When("I Make POST request to /sns/{$topicName} endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName"))
                        .andDo(print())
                Then("I get a 400 - bad request response (400) and the SNS client is not called") {
                    request.andExpect(status().isBadRequest)
                    verify { snsMessageClient wasNot called }
                }
            }
        }

        Given("A non JSON message") {
            val message = "I'm not JSON"
            When("I Make POST request to /sns/{$topicName} endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName")
                        .content(message))
                Then("I get a 415 - unsupported media type response and the SNS client is not called") {
                    request.andExpect(status().isUnsupportedMediaType)
                    verify { snsMessageClient wasNot called }
                }
            }
        }

        Given("A JSON message with a unexpected format") {
            val message = """
            {
                "message": "This message has a unexpected format"
            }
            """.trimIndent()

            When("I Make POST request to /sns/{$topicName} endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(message))
                Then("I get a 400 - bad request response and the SNS client is not called") {
                    request.andExpect(status().isBadRequest)
                    verify { snsMessageClient wasNot called }
                }
            }
        }

        Given("A simple message") {
            val message = "Hello there!"
            When("I Make POST request to /sns/{$topicName}/raw endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName/raw")
                        .content(message))
                Then("I get a 200 - OK response and the SNS client publishes the message") {
                    request.andExpect(status().isOk)
                    verify { snsMessageClient.sendMessage(topicName, message) }
                }
            }
        }

        Given("An empty request body") {
            When("I Make POST request to /sns/{$topicName}/raw endpoint") {
                val request = mockMvc.perform(post("/sns/$topicName/raw"))
                        .andDo(print())
                Then("I get a 400 - bad request response (400) and the SNS client is not called") {
                    request.andExpect(status().isBadRequest)
                    verify { snsMessageClient wasNot called }
                }
            }
        }
    }
}