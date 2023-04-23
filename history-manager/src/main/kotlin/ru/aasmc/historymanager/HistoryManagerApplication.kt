package ru.aasmc.historymanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class HistoryManagerApplication

fun main(args: Array<String>) {
    runApplication<HistoryManagerApplication>(*args)
}
