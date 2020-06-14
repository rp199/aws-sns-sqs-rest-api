package com.rp199.aws.restclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SnsSqsRestApiApplication

fun main(args: Array<String>) {
    runApplication<SnsSqsRestApiApplication>(*args)
}