package com.barofarm.gateway.controller;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<ResponseEntity<Map<String, Object>>> fallback() {
        Map<String, Object> payload = Map.of(
            "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
            "message", "Gateway fallback");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(payload));
    }
}
