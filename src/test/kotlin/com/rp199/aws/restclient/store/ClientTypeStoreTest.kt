package com.rp199.aws.restclient.store

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

internal class ClientTypeStoreTest : StringSpec({

    "getClientType should relate to each thread"{
        ClientTypeStore.setClientType(ClientType.AWS)

        val thread1 = Thread {
            ClientTypeStore.setClientType(ClientType.LOCAL)
        }

        thread1.start()
        thread1.join()
        ClientTypeStore.getClientType() shouldBe ClientType.AWS
    }

    "clear should remove clientType from the store" {
        ClientTypeStore.setClientType(ClientType.LOCAL)
        ClientTypeStore.getClientType() shouldBe ClientType.LOCAL

        ClientTypeStore.clear()
        ClientTypeStore.getClientType() should beNull()
    }
})