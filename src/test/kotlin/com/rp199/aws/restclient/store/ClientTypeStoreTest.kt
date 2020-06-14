package com.rp199.aws.restclient.store

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ClientTypeStoreTest {

    @Test
    fun `getClientType should relate to each thread`() {
        ClientTypeStore.setClientType(ClientType.AWS)

        val thread1 = Thread {
            ClientTypeStore.setClientType(ClientType.LOCAL)
        }

        thread1.start()
        thread1.join()
        Assertions.assertEquals(ClientType.AWS, ClientTypeStore.getClientType())
    }

    @Test
    fun `clear should remove clientType from the store`() {
        ClientTypeStore.setClientType(ClientType.LOCAL)
        Assertions.assertEquals(ClientType.LOCAL, ClientTypeStore.getClientType())

        ClientTypeStore.clear()
        Assertions.assertEquals(null, ClientTypeStore.getClientType())

    }
}