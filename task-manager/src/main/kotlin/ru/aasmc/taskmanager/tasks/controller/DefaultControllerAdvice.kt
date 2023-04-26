package ru.aasmc.taskmanager.tasks.controller

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.aasmc.taskmanager.tasks.exception.ExceptionResponse
import ru.aasmc.taskmanager.tasks.exception.NoSuchTaskException
import ru.aasmc.taskmanager.tasks.exception.TaskIntersectionException

private val log = LoggerFactory.getLogger(DefaultControllerAdvice::class.java)

@RestControllerAdvice
class DefaultControllerAdvice {

    @ExceptionHandler(NoSuchTaskException::class)
    fun handleNotFound(e: NoSuchTaskException): ResponseEntity<ExceptionResponse> {
        val msg = "Task not found with exception: ${e.message}"
        log.error(msg)
        val response = ExceptionResponse(msg, HttpStatus.NOT_FOUND.value())
        return ResponseEntity(response, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(TaskIntersectionException::class)
    fun handleTaskIntersection(e: TaskIntersectionException): ResponseEntity<ExceptionResponse> {
        val msg = "Task Intersection Exception: ${e.message}"
        log.error(msg)
        val response = ExceptionResponse(msg, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleInvalidRequest(e: MethodArgumentNotValidException): ResponseEntity<ExceptionResponse> {
        val msg = "Invalid Method Argument: ${e.message}"
        log.error(msg)
        val response = ExceptionResponse(msg, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

}