package com.rp199.aws.restclient.config

import com.rp199.aws.restclient.store.ClientType
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import org.springdoc.core.customizers.OpenApiCustomiser
import org.springframework.context.annotation.Configuration

@Configuration
class MyOpenApiCustomizer : OpenApiCustomiser {
    var OPERATION_GETTERS: List<(PathItem) -> Operation?> = listOf({ obj -> obj.get }, { obj -> obj.post }, { obj -> obj.delete },
            { obj -> obj.head }, { obj -> obj.options }, { obj -> obj.patch }, { obj -> obj.put })


    private fun getOperations(pathItem: PathItem): List<Operation> {
        return OPERATION_GETTERS.mapNotNull { it(pathItem) }
    }

    override fun customise(openApi: OpenAPI) {
        val schema = Schema<ClientType>().type("string")
        schema.enum = ClientType.values().toList()

        openApi.paths.values.flatMap { getOperations(it) }.forEach {
            it.addParametersItem(Parameter().`in`("header").required(false)
                    .description("Specifies which client to use in the request: 'LOCAL' to use a local client " +
                            "or 'AWS' to use the real aws client with the environment credentials")
                    .name("clientType").schema(schema))
        }
    }
}