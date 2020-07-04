package com.rp199.aws.restclient.messaging

import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.model.ListTopicsRequest
import com.amazonaws.services.sns.model.ListTopicsResult
import com.amazonaws.services.sns.model.PublishRequest
import com.amazonaws.services.sns.model.Topic
import com.rp199.aws.restclient.utils.TestUtils.Companion.generateDummyArn
import com.rp199.aws.restclient.utils.TestUtils.Companion.shouldBeIgnoringCommonHeadersSNS
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.*

internal class SnsMessageClientTest : BehaviorSpec() {

    private val amazonSNS: AmazonSNS = mockk(relaxed = true)
    private val snsMessageClient = SnsMessageClient(amazonSNS)

    private val topicName = "my-topic"
    private val dummyTopicArn = generateDummyArn(topicName)

    override fun beforeTest(testCase: TestCase) {
        super.beforeTest(testCase)

        if (testCase.isTopLevel()) {
            clearAllMocks()
        }
    }

    init {
        Given("A message with message attributes") {
            val message = "Greetings human"
            val messageAttributeMap = mapOf("someAttribute" to "some attribute Value",
                    "anotherAttribute" to "another value")
            every { amazonSNS.listTopics(any<ListTopicsRequest>()) } returns ListTopicsResult()
                    .withTopics(Topic().withTopicArn(dummyTopicArn))

            When("I publish the message to the topic") {
                snsMessageClient.sendMessage(topicName, message, messageAttributeMap)
                Then("The message is successfully published") {
                    val captor = slot<PublishRequest>()
                    verify { amazonSNS.publish(capture(captor)) }
                    val publishRequest = captor.captured

                    publishRequest.message shouldBe message
                    publishRequest.topicArn shouldBe dummyTopicArn
                    publishRequest.messageAttributes shouldBeIgnoringCommonHeadersSNS messageAttributeMap
                }
            }
        }

        Given("A message without message attributes") {
            val message = "Greetings human"
            every { amazonSNS.listTopics(any<ListTopicsRequest>()) } returns ListTopicsResult()
                    .withTopics(Topic().withTopicArn(dummyTopicArn))

            When("I publish the message to the topic") {
                snsMessageClient.sendMessage(topicName, message)
                Then("The message is successfully published") {
                    val captor = slot<PublishRequest>()
                    verify { amazonSNS.publish(capture(captor)) }
                    val publishRequest = captor.captured

                    publishRequest.message shouldBe message
                    publishRequest.topicArn shouldBe dummyTopicArn
                    publishRequest.messageAttributes shouldBeIgnoringCommonHeadersSNS emptyMap()
                }
            }
        }

        Given("That there are no topics") {
            every { amazonSNS.listTopics() } returns ListTopicsResult()
                    .withTopics(emptyList())
            When("I list the topics") {
                val result = snsMessageClient.listTopics()
                Then("I get no topics") {
                    verify { amazonSNS.listTopics() }
                    result.shouldBeEmpty()
                }
            }
        }

        Given("That there are topics") {
            val topics = listOf(Topic().withTopicArn(generateDummyArn("my-topic1")),
                    Topic().withTopicArn(generateDummyArn("my-topic2")))
            every { amazonSNS.listTopics() } returns ListTopicsResult()
                    .withTopics(topics)
            When("I list the topics") {
                val result = snsMessageClient.listTopics()
                Then("I get the arn for all the topics") {
                    verify { amazonSNS.listTopics() }
                    result shouldBe topics.map { it.topicArn }
                }
            }
        }
    }
}