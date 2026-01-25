package com.barofarm.opabundle.web;

import com.barofarm.opabundle.application.OpaBundleService;
import java.nio.file.Path;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/opa")
// [0] OPA 번들 파일을 HTTP로 제공하는 REST 컨트롤러 클래스.
public class BundleController {

    // [1] HTTP endpoint to fetch the latest OPA bundle.
    private final OpaBundleService opaBundleService;

    public BundleController(OpaBundleService opaBundleService) {
        this.opaBundleService = opaBundleService;
    }

    // [2] Serve the bundle archive as a gzipped file download.
    @GetMapping("/bundle")
    public ResponseEntity<Resource> getBundle() {
        opaBundleService.ensureBundleExists();
        Path bundlePath = opaBundleService.getBundlePath();
        Resource resource = new FileSystemResource(bundlePath);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bundle.tar.gz\"")
            .contentType(MediaType.parseMediaType("application/gzip"))
            .body(resource);
    }
}
