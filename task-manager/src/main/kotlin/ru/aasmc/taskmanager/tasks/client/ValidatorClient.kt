package ru.aasmc.taskmanager.tasks.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import ru.aasmc.taskmanager.tasks.dto.DeleteTaskValidationRequest

@FeignClient(name = "validator-client", url = "\${urls.validator}")
interface ValidatorClient {

    @PostMapping("/delete")
    fun deleteValidationInfo(request: DeleteTaskValidationRequest)

}