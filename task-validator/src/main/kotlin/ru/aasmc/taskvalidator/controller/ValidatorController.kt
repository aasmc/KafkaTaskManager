package ru.aasmc.taskvalidator.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.aasmc.taskvalidator.dto.DeleteTaskValidationRequest
import ru.aasmc.taskvalidator.service.ValidationService

@RestController
@RequestMapping("/delete")
class ValidatorController(
        private val service: ValidationService
) {

    companion object {
        private val log = LoggerFactory.getLogger(ValidatorController::class.java)
    }

    @PostMapping
    fun deleteValidationInfo(@RequestBody deleteRequest: DeleteTaskValidationRequest) {
        log.info("POST /delete with request: $deleteRequest")
        service.deleteValidationInfo(deleteRequest)
    }

}