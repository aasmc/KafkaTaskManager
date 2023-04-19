package ru.aasmc.taskvalidator.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.aasmc.taskvalidator.dto.DeleteTaskValidationRequest
import ru.aasmc.taskvalidator.dto.ValidationRequest
import ru.aasmc.taskvalidator.dto.ValidationResponse
import ru.aasmc.taskvalidator.dto.ValidationResult
import ru.aasmc.taskvalidator.model.Range
import ru.aasmc.taskvalidator.repository.RangeRepository
import java.time.LocalDateTime

@Service
class ValidationService(
        private val repo: RangeRepository
) {

    companion object {
        private val log = LoggerFactory.getLogger(ValidationService::class.java)
    }

    fun validate(request: ValidationRequest): ValidationResponse {
        log.info("Started validation of request: $request")
        val overlappings =
                repo.findAllOverlappings(request.taskStartTime, request.taskEndTime)
        log.info("Found ranges with intersection: $overlappings")
        val result = if (overlappings.isEmpty()) {
            ValidationResult.SUCCESS
        } else {
            ValidationResult.FAILURE
        }
        if (rangeValid(result)) {
            saveRange(request.taskStartTime, request.taskEndTime)
        }
        return ValidationResponse(request.taskId, result)
    }

    fun deleteValidationInfo(deleteRequest: DeleteTaskValidationRequest) {
        log.info("Deleting validation information for request: $deleteRequest")
        repo.deleteRangeByEndAndStart(deleteRequest.startTime, deleteRequest.endTime)
    }

    private fun rangeValid(result: ValidationResult): Boolean {
        return result == ValidationResult.SUCCESS
    }
    private fun saveRange(start: LocalDateTime, end: LocalDateTime) {
        repo.save(Range(start = start, end = end))
    }

}