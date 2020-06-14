package com.rp199.aws.restclient.controller

import com.beust.klaxon.Klaxon
import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SqsMessageClient
import com.rp199.aws.restclient.store.ClientType
import io.mockk.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@ExtendWith(SpringExtension::class)
@WebMvcTest(SqsController::class)
internal class SqsControllerTest {

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

    @BeforeEach
    fun clearMockState() {
        clearAllMocks()
    }

    @Test
    fun `Spring context loaded`() {
    }

    @Test
    fun `GET list endpoint should return a list of queues when there are queues`() {
        //Given
        val expectedTopicListResponse = listOf("my-queue1", "my-queue2", "my-queue3")
        every { sqsMessageClient.listQueues() } returns expectedTopicListResponse

        //When
        val response = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/list"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()

        //Then
        Assertions.assertEquals(Klaxon().toJsonString(expectedTopicListResponse).replace(" ", ""), response.response.contentAsString)
        verify { sqsMessageClient.listQueues() }
    }

    @Test
    fun `GET list endpoint should return an empty list of queue when there are no queues`() {
        //Given
        every { sqsMessageClient.listQueues() } returns emptyList()

        //When
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/list"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()

        //Then
        Assertions.assertEquals(Klaxon().toJsonString(emptyList<String>()), result.response.contentAsString)
        verify { sqsMessageClient.listQueues() }
    }

    @Test
    fun `POST queueName endpoint should publish message with message attributes into the given queue`() {
        //Given
        val queueName = "my-queue"
        val publishMessageRequest = PublishMessageRequest("Hello there!",
                mapOf("someMessageAttribute" to "666", "anotherMessageAttribute" to "someValue"))

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(publishMessageRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
    }

    @Test
    fun `POST queueName endpoint should publish message without message attributes into the given queue`() {
        //Given
        val queueName = "my-queue"
        val publishMessageRequest = PublishMessageRequest("Hello there!", null)

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(publishMessageRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient.sendMessage(queueName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
    }


    @Test
    fun `POST queueName endpoint should fail when the request body is missing`() {
        //Given
        val queueName = "my-queue"

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient wasNot called }
    }

    @Test
    fun `POST queueName endpoint should fail when the request body is not JSON`() {
        //Given
        val queueName = "my-queue"

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName").content("I'm not JSON'"))
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient wasNot called }
    }

    @Test
    fun `POST queueName endpoint should fail when the request body is invalid`() {
        //Given
        val queueName = "my-queue"

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
            "message": "This message has a unexpected format"
            }
        """.trimIndent()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient wasNot called }
    }

    @Test
    fun `POST queueName raw endpoint should publish raw message into the given queue`() {
        //Given
        val queueName = "my-queue"
        val message = "Hello there!"

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName/raw")
                .content(message))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient.sendMessage(queueName, message) }
    }


    @Test
    fun `POST queueName raw endpoint should fail when the request body is missing`() {
        //Given
        val queueName = "my-queue"

        //When
        mockMvc.perform(MockMvcRequestBuilders.post("/sqs/$queueName/raw"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
        //Then
        verify { sqsMessageClient wasNot called }
    }

    @Test
    fun `GET queueName endpoint should return the message when the queue has messages`() {
        //Given
        val queueName = "my-queue"
        val message = "Hello there!"

        every { sqsMessageClient.receiveMessage(queueName) } returns message

        //When
        val result = mockMvc.perform(MockMvcRequestBuilders.get("/sqs/$queueName"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()

        //Then
        Assertions.assertEquals(message, result.response.contentAsString)
        verify { sqsMessageClient.receiveMessage(queueName) }
    }

    @Test
    fun `GET queueName endpoint should return 404 not found when the queue is empty`() {
        //Given
        val queueName = "my-queue"
        every { sqsMessageClient.receiveMessage(queueName) } returns null

        //When
        mockMvc.perform(MockMvcRequestBuilders.get("/sqs/$queueName"))
                .andExpect(MockMvcResultMatchers.status().isNotFound)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()

        //Then
        verify { sqsMessageClient.receiveMessage(queueName) }
    }

    @Test
    fun `DELETE queueName endpoint should purge the given queue`() {
        //Given
        val queueName = "my-queue"

        //When
        mockMvc.perform(MockMvcRequestBuilders.delete("/sqs/$queueName"))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andDo(MockMvcResultHandlers.print())
                .andReturn()

        //Then
        verify { sqsMessageClient.purgeQueue(queueName) }
    }
}