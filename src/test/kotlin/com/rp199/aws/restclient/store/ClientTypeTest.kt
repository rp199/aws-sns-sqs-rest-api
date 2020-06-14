package com.rp199.aws.restclient.store

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.NullAndEmptySource

internal class ClientTypeTest {

    @ParameterizedTest
    @NullAndEmptySource
    fun `fromString should return default value for null and empty values`(nullOrEmpty: String?) {
        Assertions.assertEquals(ClientType.AWS, ClientType.fromString(nullOrEmpty))
    }

    @ParameterizedTest
    @CsvSource("local,LOCAL", "aws,AWS", "LOCAL,LOCAL", "AWS,AWS")
    fun `fromString should map correctly from the matching string ignoring case`(clientTypeString: String,
                                                                                 expectedClientType: ClientType) {
        Assertions.assertEquals(expectedClientType, ClientType.fromString(clientTypeString))
    }
}