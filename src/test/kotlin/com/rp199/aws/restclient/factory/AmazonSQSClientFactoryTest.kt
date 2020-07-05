package com.rp199.aws.restclient.factory

import com.rp199.aws.restclient.config.AwsConfiguration
import com.rp199.aws.restclient.store.ClientType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AmazonSQSClientFactoryTest : StringSpec({


    "should create local client when Client Type is LOCAL" {
        val amazonSqsClientFactory = AmazonSQSClientFactory("anEndpoint", "aRegion", AwsConfiguration()
                .createLocalAWSCredentialsProvider())

        val result = amazonSqsClientFactory.createClient(ClientType.LOCAL)

        result shouldBe amazonSqsClientFactory.localClient

    }

    "should create default client when Client Type is AWS" {
        val amazonSqsClientFactory = AmazonSNSClientFactory("anEndpoint", "aRegion", AwsConfiguration()
                .createLocalAWSCredentialsProvider())

        val result = amazonSqsClientFactory.createClient(ClientType.AWS)

        result shouldBe amazonSqsClientFactory.defaultClient
    }
})
