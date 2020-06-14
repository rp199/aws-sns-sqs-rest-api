package com.rp199.aws.restclient.domain

data class PublishMessageRequest(val payload: String, val messageAttributes: Map<String, String>?)