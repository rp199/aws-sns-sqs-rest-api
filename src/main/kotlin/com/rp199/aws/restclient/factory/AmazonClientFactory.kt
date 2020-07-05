package com.rp199.aws.restclient.factory

import com.rp199.aws.restclient.store.ClientType

abstract class AmazonClientFactory<T> {
    val localClient: T by lazy { createLocalClient() }
    val defaultClient: T by lazy { createDefaultClient() }

    fun createClient(clientType: ClientType): T {
        return when (clientType) {
            ClientType.LOCAL -> localClient
            else -> defaultClient
        }
    }

    protected abstract fun createDefaultClient(): T

    protected abstract fun createLocalClient(): T
}