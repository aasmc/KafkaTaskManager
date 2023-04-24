package ru.aasmc.historymanager.testutil

import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import java.util.Objects

private val containers = hashMapOf<String, KafkaContainer>()

fun reuseContainer(name: String): KafkaContainer {
    if (containers.containsKey(name) && Objects.nonNull(containers[name])) {
        return containers[name]!!
    }
    val container = KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.1"))
    container.start()
    containers[name] = container
    return container
}