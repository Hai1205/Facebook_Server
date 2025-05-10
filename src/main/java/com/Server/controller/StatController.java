package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.api.StatApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stats")
public class StatController {
    @Autowired
    private StatApi statApi;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getGeneralStat() {
        Response response = statApi.getGeneralStat();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-popular-post")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getPopularPostStat() {
        Response response = statApi.getPopularPostStat();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-top-users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getTopUsersStat() {
        Response response = statApi.getTopUsersStat();
        
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}