package ru.aasmc.taskmanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableKafka
@EnableFeignClients
class TaskManagerApplication

fun main(args: Array<String>) {
    runApplication<TaskManagerApplication>(*args)
}
