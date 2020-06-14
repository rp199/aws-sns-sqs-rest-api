package com.rp199.aws.restclient.controller

import com.beust.klaxon.Klaxon
import com.rp199.aws.restclient.domain.PublishMessageRequest
import com.rp199.aws.restclient.messaging.SnsMessageClient
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(SnsController::class)
internal class SnsControllerTest {

    @TestConfiguration
    class SnsControllerTestConfiguration {
        @Bean
        fun snsMessageClient() = mockk<SnsMessageClient>(relaxed = true)

        @Bean
        fun clientType() = mockk<ClientType>(relaxed = true)
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var snsMessageClient: SnsMessageClient

    @BeforeEach
    fun clearMockState() {
        clearAllMocks()
    }

    @Test
    fun `Spring context loaded`() {
    }

    @Test
    fun `GET list endpoint should return a list of topic when there are topics`() {
        //Given
        val expectedTopicListResponse = listOf("my-topic1", "my-topic2", "my-topic3")
        every { snsMessageClient.listTopics() } returns expectedTopicListResponse

        //When
        val response = mockMvc.perform(get("/sns/list"))
                .andExpect(status().isOk)
                .andDo(print())
                .andReturn()

        //Then
        Assertions.assertEquals(Klaxon().toJsonString(expectedTopicListResponse).replace(" ", ""), response.response.contentAsString)
        verify { snsMessageClient.listTopics() }
    }

    @Test
    fun `GET list endpoint should return an empty list of topic when there are no topics`() {
        //Given
        every { snsMessageClient.listTopics() } returns emptyList()

        //When
        val result = mockMvc.perform(get("/sns/list"))
                .andExpect(status().isOk)
                .andDo(print())
                .andReturn()

        //Then
        Assertions.assertEquals(Klaxon().toJsonString(emptyList<String>()), result.response.contentAsString)
        verify { snsMessageClient.listTopics() }
    }

    @Test
    fun `POST topicName endpoint should publish message with message attributes into the given topic`() {
        //Given
        val topicName = "my-topic"
        val publishMessageRequest = PublishMessageRequest("Hello there!",
                mapOf("someMessageAttribute" to "666", "anotherMessageAttribute" to "someValue"))

        //When
        mockMvc.perform(post("/sns/$topicName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(publishMessageRequest)))
                .andExpect(status().isOk)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
    }

    @Test
    fun `POST topicName endpoint should publish message without message attributes into the given topic`() {
        //Given
        val topicName = "my-topic"
        val publishMessageRequest = PublishMessageRequest("Hello there!", null)

        //When
        mockMvc.perform(post("/sns/$topicName")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Klaxon().toJsonString(publishMessageRequest)))
                .andExpect(status().isOk)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient.sendMessage(topicName, publishMessageRequest.payload, publishMessageRequest.messageAttributes) }
    }


    @Test
    fun `POST topicName endpoint should fail when the request body is missing`() {
        //Given
        val topicName = "my-topic"

        //When
        mockMvc.perform(post("/sns/$topicName"))
                .andExpect(status().isBadRequest)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient wasNot called }
    }

    @Test
    fun `POST topicName endpoint should fail when the request body is not JSON`() {
        //Given
        val topicName = "my-topic"

        //When
        mockMvc.perform(post("/sns/$topicName").content("I'm not JSON'"))
                .andExpect(status().isUnsupportedMediaType)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient wasNot called }
    }

    @Test
    fun `POST topicName endpoint should fail when the request body is invalid`() {
        //Given
        val topicName = "my-topic"

        //When
        mockMvc.perform(post("/sns/$topicName")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
            {
            "message": "This message has a unexpected format"
            }
        """.trimIndent()))
                .andExpect(status().isBadRequest)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient wasNot called }
    }

    @Test
    fun `POST topicName raw endpoint should publish raw message into the given topic`() {
        //Given
        val topicName = "my-topic"
        val message = "Hello there!"

        //When
        mockMvc.perform(post("/sns/$topicName/raw")
                .content(message))
                .andExpect(status().isOk)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient.sendMessage(topicName, message) }
    }


    @Test
    fun `POST topicName raw endpoint should fail when the request body is missing`() {
        //Given
        val topicName = "my-topic"

        //When
        mockMvc.perform(post("/sns/$topicName/raw"))
                .andExpect(status().isBadRequest)
                .andDo(print())
                .andReturn()
        //Then
        verify { snsMessageClient wasNot called }
    }
}