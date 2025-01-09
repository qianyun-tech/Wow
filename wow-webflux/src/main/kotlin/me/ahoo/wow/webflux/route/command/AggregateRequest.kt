/*
 * Copyright [2021-present] [ahoo wang <ahoowang@qq.com> (https://github.com/Ahoo-Wang)].
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.ahoo.wow.webflux.route.command

import me.ahoo.wow.api.modeling.TenantId
import me.ahoo.wow.command.wait.CommandStage
import me.ahoo.wow.infra.ifNotBlank
import me.ahoo.wow.modeling.matedata.AggregateMetadata
import me.ahoo.wow.openapi.RoutePaths
import me.ahoo.wow.openapi.command.CommandHeaders
import me.ahoo.wow.serialization.MessageRecords
import org.springframework.web.reactive.function.server.ServerRequest
import java.time.Duration
import java.util.*

fun ServerRequest.getTenantId(aggregateMetadata: AggregateMetadata<*, *>): String? {
    aggregateMetadata.staticTenantId.ifNotBlank<String> {
        return it
    }
    pathVariables()[MessageRecords.TENANT_ID].ifNotBlank<String> {
        return it
    }
    headers().firstHeader(CommandHeaders.TENANT_ID).ifNotBlank<String> {
        return it
    }
    return null
}

fun ServerRequest.getTenantIdOrDefault(aggregateMetadata: AggregateMetadata<*, *>): String {
    return getTenantId(aggregateMetadata) ?: return TenantId.DEFAULT_TENANT_ID
}

fun ServerRequest.getAggregateId(): String? {
    headers().firstHeader(CommandHeaders.AGGREGATE_ID).ifNotBlank<String> {
        return it
    }
    pathVariables()[RoutePaths.ID_KEY].ifNotBlank<String> {
        return it
    }
    return null
}

fun ServerRequest.getLocalFirst(): Boolean? {
    headers().firstHeader(CommandHeaders.LOCAL_FIRST).ifNotBlank<String> {
        return it.toBoolean()
    }
    return null
}

fun ServerRequest.getCommandStage(): CommandStage {
    return headers().firstHeader(CommandHeaders.WAIT_STAGE).ifNotBlank { stage ->
        CommandStage.valueOf(stage.uppercase(Locale.getDefault()))
    } ?: CommandStage.PROCESSED
}

fun ServerRequest.getWaitContext(): String {
    return headers().firstHeader(CommandHeaders.WAIT_CONTEXT).orEmpty()
}

fun ServerRequest.getWaitProcessor(): String {
    return headers().firstHeader(CommandHeaders.WAIT_PROCESSOR).orEmpty()
}

fun ServerRequest.getWaitTimeout(default: Duration = DEFAULT_TIME_OUT): Duration {
    return headers().firstHeader(CommandHeaders.WAIT_TIME_OUT)?.toLongOrNull()?.let {
        Duration.ofMillis(it)
    } ?: default
}
