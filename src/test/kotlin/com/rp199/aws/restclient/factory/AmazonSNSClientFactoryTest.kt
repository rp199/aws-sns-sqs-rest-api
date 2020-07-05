package com.rp199.aws.restclient.factory

import com.rp199.aws.restclient.config.AwsConfiguration
import com.rp199.aws.restclient.store.ClientType
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class AmazonSNSClientFactoryTest : StringSpec({

    "should create local client when Client Type is LOCAL" {
        val amazonSNSClientFactory = AmazonSNSClientFactory("anEndpoint", "aRegion", AwsConfiguration()
                .createLocalAWSCredentialsProvider())

        val result = amazonSNSClientFactory.createClient(ClientType.LOCAL)

        result shouldBe amazonSNSClientFactory.localClient

    }

    "should create default client when Client Type is AWS" {
        val amazonSNSClientFactory = AmazonSNSClientFactory("anEndpoint", "aRegion", AwsConfiguration()
                .createLocalAWSCredentialsProvider())

        val result = amazonSNSClientFactory.createClient(ClientType.AWS)

        result shouldBe amazonSNSClientFactory.defaultClient
    }
})
