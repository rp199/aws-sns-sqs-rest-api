package com.rp199.aws.restclient.utils

import com.amazonaws.services.sns.model.MessageAttributeValue
import io.kotest.matchers.shouldBe

class TestUtils {
    companion object {
        fun generateDummyArn(resourceName: String) = """arn:aws:sns:us-east-1:111122223333:$resourceName"""

        infix fun Map<String, com.amazonaws.services.sqs.model.MessageAttributeValue>.shouldBeIgnoringCommonHeadersSQS(expected: Map<String, String>) {
            shouldBeIgnoringCommonHeaders(expected, this, ::createSQSMessageAttribute)
        }

        infix fun Map<String, MessageAttributeValue>.shouldBeIgnoringCommonHeadersSNS(expected: Map<String, String>) {
            shouldBeIgnoringCommonHeaders(expected, this, ::createSNSMessageAttribute)
        }

        private fun <T> shouldBeIgnoringCommonHeaders(expected: Map<String, String>, actual: Map<String, T>, messageAttributeCreator: (s: String) -> T) {
            actual.size shouldBe expected.size.plus(3)
            removeCommonMessageAttributes(actual) shouldBe mapToAWSAttributeValues(expected, messageAttributeCreator)
        }

        private fun <T> mapToAWSAttributeValues(messageAttributeStringMap: Map<String, String>, messageAttributeCreator: (s: String) -> T): Map<String, T> {
            return messageAttributeStringMap.mapValues {
                messageAttributeCreator(it.value)
            }
        }

        private fun <T> removeCommonMessageAttributes(messageAttributeValueMap: Map<String, T>): Map<String, T> {
            val commonHeaders = arrayListOf("id", "contentType", "timestamp")
            return messageAttributeValueMap.filterKeys { !commonHeaders.contains(it) }
        }

        private fun createSNSMessageAttribute(value: String) = MessageAttributeValue()
                .withStringValue(value)
                .withDataType("String")

        private fun createSQSMessageAttribute(value: String) = com.amazonaws.services.sqs.model.MessageAttributeValue()
                .withStringValue(value)
                .withDataType("String")

    }
}