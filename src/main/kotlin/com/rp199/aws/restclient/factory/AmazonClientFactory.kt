package com.rp199.aws.restclient.factory

import com.rp199.aws.restclient.store.ClientType

interface AmazonClientFactory<T> {

    fun createClient(clientType: ClientType): T {
        return when (clientType) {
            ClientType.LOCAL -> createLocalClient()
            else -> createDefaultClient()
        }
    }

    fun createDefaultClient(): T

    fun createLocalClient(): T
}