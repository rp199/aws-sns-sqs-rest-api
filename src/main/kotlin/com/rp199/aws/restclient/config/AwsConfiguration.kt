package com.rp199.aws.restclient.config

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sqs.AmazonSQSAsync
import com.rp199.aws.restclient.factory.AmazonSNSClientFactory
import com.rp199.aws.restclient.factory.AmazonSQSClientFactory
import com.rp199.aws.restclient.store.ClientType
import com.rp199.aws.restclient.store.ClientTypeStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.web.context.annotation.RequestScope

@Configuration
class AwsConfiguration {
    private val dummyAccessKey = "dummyKey"
    private val dummySecretKey = "dummySecretKey"

    @Bean
    @Qualifier("defaultClientType")
    fun defaultClientType(@Value("\${aws.default-mode}") defaultClient: String?): ClientType {
        return ClientType.fromString(defaultClient)
    }

    @Bean
    @RequestScope
    @Primary
    fun createAmazonSNS(amazonSNSClientFactory: AmazonSNSClientFactory, defaultClientType: ClientType): AmazonSNS {
        return amazonSNSClientFactory.createClient(ClientTypeStore.getClientType() ?: defaultClientType)

    }

    @Bean
    @RequestScope
    @Primary
    fun createAmazonAsyncSQS(amazonSQSClientFactory: AmazonSQSClientFactory, defaultClientType: ClientType): AmazonSQSAsync {
        return amazonSQSClientFactory.createClient(ClientTypeStore.getClientType() ?: defaultClientType)
    }

    @Bean
    @Primary
    fun createLocalAWSCredentialsProvider(): AWSCredentialsProvider = AWSStaticCredentialsProvider(
            BasicAWSCredentials(dummyAccessKey, dummySecretKey)
    )
}