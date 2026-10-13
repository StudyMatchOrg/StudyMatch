package pt.upt.studymatch.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.upt.studymatch.dto.HealthResponse;
import pt.upt.studymatch.service.HealthService;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final HealthService healthService;

    public HealthController(HealthService healthService) {
        this.healthService = healthService;
    }

    @GetMapping
    public HealthResponse health() {
        return healthService.check();
    }
}
