package com.moneta.hub.moneta.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/health")
@RequiredArgsConstructor
@Slf4j
public class HealthCheckController {

    @GetMapping
    public ResponseEntity<Object> healthCheck() {
        log.info(" > > > GET /api/v1/health");
        log.info("Backend is up and running >>> {}.", LocalDateTime.now());
        log.info(" < < < GET /api/v1/health");
        return ResponseEntity.ok().build();
    }
}
