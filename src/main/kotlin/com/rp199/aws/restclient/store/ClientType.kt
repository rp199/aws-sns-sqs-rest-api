package com.rp199.aws.restclient.store

enum class ClientType {
    LOCAL, AWS;

    companion object {
        fun fromString(stringValue: String?): ClientType {
            return values().firstOrNull { it.name == stringValue?.toUpperCase() } ?: AWS
        }
    }
}

