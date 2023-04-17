package ru.aasmc.historymanager

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HistoryManagerApplication

fun main(args: Array<String>) {
    runApplication<HistoryManagerApplication>(*args)
}
