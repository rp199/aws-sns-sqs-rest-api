package com.rp199.aws.restclient.factory

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AmazonSNSClientFactory(
        @Value("\${aws.sns.local.endpoint}") private val localEndpoint: String,
        @Value("\${aws.sns.local.region}") private val localRegion: String,
        private val awsDummyCredentialsProvider: AWSCredentialsProvider
) : AmazonClientFactory<AmazonSNS> {

    override fun createDefaultClient(): AmazonSNS {
        return AmazonSNSClientBuilder.defaultClient()
    }

    override fun createLocalClient(): AmazonSNS {
        return AmazonSNSClientBuilder.standard()
                .withCredentials(awsDummyCredentialsProvider)
                .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(localEndpoint,
                        localRegion))
                .build()
    }
}