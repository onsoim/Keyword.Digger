package com.onsoim.keyword.digger.system.api

import com.onsoim.keyword.digger.system.api.dto.HealthResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {
    
    @GetMapping("/api/v1/health")
    fun getHealth(): HealthResponse {
        return HealthResponse(
            status = "UP",
            application = "Keyword-Digger",
        )
    }
}