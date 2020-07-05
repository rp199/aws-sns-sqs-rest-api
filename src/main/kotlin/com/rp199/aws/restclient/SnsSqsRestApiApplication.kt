package com.rp199.aws.restclient

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.info.Info
import io.swagger.v3.oas.annotations.info.License
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@OpenAPIDefinition(info = Info(title = "SNS/SQS Rest API", description = "A spring boot REST API for interacting with AWS SNS and SQS written in Kotlin",
        version = "v1", license = License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")))
@SpringBootApplication
class SnsSqsRestApiApplication

fun main(args: Array<String>) {
    runApplication<SnsSqsRestApiApplication>(*args)
}