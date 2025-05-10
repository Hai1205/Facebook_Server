package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.api.NotiApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotiController {
    @Autowired
    private NotiApi notiApi;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllNoti(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "order", defaultValue = "asc") String order
    ) {
        Response response = notiApi.getAllNoti(page, limit, sort, order);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-noti/{NotiId}")
    public ResponseEntity<Response> deleteNotiById(@PathVariable("NotiId") String NotiId) {
        Response response = notiApi.deleteNoti(NotiId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-user-notifications/{userId}")
    public ResponseEntity<Response> deleteUserNotifications(@PathVariable("userId") String userId) {
        Response response = notiApi.deleteUserNotifications(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-notifications/{userId}")
    public ResponseEntity<Response> getUserNotifications(@PathVariable("userId") String userId) {
        Response response = notiApi.getUserNotifications(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
