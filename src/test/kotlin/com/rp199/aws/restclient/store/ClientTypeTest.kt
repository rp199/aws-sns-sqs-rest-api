package com.rp199.aws.restclient.store

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class ClientTypeTest : StringSpec({

    "fromString should return default value for null and empty values"{
        listOf("",
                null
        ).map { ClientType.fromString(it) shouldBe ClientType.AWS }
    }

    "fromString should map correctly from the matching string ignoring case"{
        listOf(
                "local" to ClientType.LOCAL,
                "LOCAL" to ClientType.LOCAL,
                "aws" to ClientType.AWS,
                "AWS" to ClientType.AWS
        ).map { (input, expected) -> ClientType.fromString(input) shouldBe expected }
    }
})