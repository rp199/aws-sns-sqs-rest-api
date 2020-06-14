package com.rp199.aws.restclient.factory

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AmazonSQSClientFactory(
        @Value("\${aws.sqs.local.endpoint}") private val endpoint: String,
        @Value("\${aws.sqs.local.region}") private val localRegion: String,
        private val awsDummyCredentialsProvider: AWSCredentialsProvider) : AmazonClientFactory<AmazonSQSAsync> {

    override fun createDefaultClient(): AmazonSQSAsync {
        return AmazonSQSAsyncClientBuilder.defaultClient()
    }

    override fun createLocalClient(): AmazonSQSAsync {
        return AmazonSQSAsyncClientBuilder.standard()
                .withCredentials(awsDummyCredentialsProvider)
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint,
                        localRegion))
                .build()
    }
}