package com.technicel.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebAppManifestController {

    @GetMapping("/manifest.webmanifest")
    public ResponseEntity<ClassPathResource> manifest() {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/manifest+json"))
                .body(new ClassPathResource("static/manifest.webmanifest"));
    }
}
