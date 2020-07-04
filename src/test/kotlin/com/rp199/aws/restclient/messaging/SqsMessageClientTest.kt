package com.rp199.aws.restclient.messaging

import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.model.*
import com.rp199.aws.restclient.utils.TestUtils.Companion.shouldBeIgnoringCommonHeadersSQS
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.*

internal class SqsMessageClientTest : BehaviorSpec() {

    private val amazonSQS: AmazonSQSAsync = mockk(relaxed = true)
    private val sqsMessageClient = SqsMessageClient(amazonSQS)

    private val queueName = "my-queue"
    private val queueUrl = "http://$queueName.com"

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

            When("I publish the message to the queue") {
                sqsMessageClient.sendMessage(queueName, message, messageAttributeMap)
                Then("The message is successfully published") {
                    val captor = slot<SendMessageRequest>()
                    verify { amazonSQS.sendMessage(capture(captor)) }
                    val sendMessageRequest = captor.captured

                    sendMessageRequest.messageBody shouldBe message
                    sendMessageRequest.messageAttributes shouldBeIgnoringCommonHeadersSQS messageAttributeMap
                }
            }
        }

        Given("A message without message attributes") {
            val message = "Greetings human"

            When("I publish the message to the queue") {
                sqsMessageClient.sendMessage(queueName, message)
                Then("The message is successfully published") {
                    val captor = slot<SendMessageRequest>()
                    verify { amazonSQS.sendMessage(capture(captor)) }
                    val sendMessageRequest = captor.captured

                    sendMessageRequest.messageBody shouldBe message
                    sendMessageRequest.messageAttributes shouldBeIgnoringCommonHeadersSQS emptyMap()
                }
            }
        }

        Given("That there are no queues") {
            every { amazonSQS.listQueues() } returns ListQueuesResult()
                    .withQueueUrls(emptyList())

            When("I list the queues") {
                val result = sqsMessageClient.listQueues()
                Then("I get no queues") {
                    verify { amazonSQS.listQueues() }
                    result.shouldBeEmpty()
                }
            }
        }

        Given("That there are queues") {
            val queueNames = listOf("my-queue1", "my-queue2", "my-queue3")
            every { amazonSQS.listQueues() } returns ListQueuesResult()
                    .withQueueUrls(queueNames)

            When("I list the queues") {
                val result = sqsMessageClient.listQueues()
                Then("I get the queue names for all the queues") {
                    verify { amazonSQS.listQueues() }
                    result shouldBe queueNames
                }
            }
        }

        Given("The queue have a message") {
            val message = Message().withBody("Hi there")
            every {
                amazonSQS.receiveMessage(any<ReceiveMessageRequest>())
            } returns ReceiveMessageResult().withMessages(message)

            When("I poll the message from the queue") {
                val result = sqsMessageClient.receiveMessage(queueName)
                Then("I get the message") {
                    verify { amazonSQS.receiveMessage(any<ReceiveMessageRequest>()) }
                    result shouldBe message.body
                }
            }
        }

        Given("The queue have no message") {
            every {
                amazonSQS.receiveMessage(any<ReceiveMessageRequest>())
            } returns ReceiveMessageResult()

            When("I poll the message from the queue") {
                val result = sqsMessageClient.receiveMessage(queueName)
                Then("I get no message") {
                    verify { amazonSQS.receiveMessage(any<ReceiveMessageRequest>()) }
                    result.shouldBeNull()
                }
            }

            Given("Queue exists") {
                every { amazonSQS.getQueueUrl(queueName) } returns GetQueueUrlResult().withQueueUrl(queueUrl)
                When("I make the purge request") {
                    sqsMessageClient.purgeQueue(queueName)
                    Then("The queue is purged") {
                        verify { amazonSQS.purgeQueue(PurgeQueueRequest(queueUrl)) }
                    }
                }
            }
        }
    }
}