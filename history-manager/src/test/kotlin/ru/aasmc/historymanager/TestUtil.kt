package ru.aasmc.historymanager

import java.nio.charset.StandardCharsets

fun loadJsonFromFile(filePath: String): String {
    return ClassLoader.getSystemResourceAsStream(filePath).use { stream ->
        stream?.let {
            String(it.readAllBytes(), StandardCharsets.UTF_8)
        }
    } ?: throw RuntimeException("Error reading contents of file $filePath")
}