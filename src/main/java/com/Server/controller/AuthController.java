package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.api.AuthApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthApi authApi;

    @PostMapping("/register")
    public ResponseEntity<Response> register(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("gender") String gender,
            @RequestParam("fullName") String fullName,
            @RequestParam("dateOfBirth") String dateOfBirth) {
        Response response = authApi.register(email, password, gender, fullName, dateOfBirth);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response> login(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            HttpServletResponse response) {
        Response loginResponse = authApi.login(email, password);

        boolean isActive = loginResponse.getUser().getStatus().equals("ACTIVE");
        boolean isOk = loginResponse.getStatusCode() == 200;

        if (isOk && isActive) {
            int SevenDays = 7 * 24 * 60 * 60;
            Cookie jwtCookie = new Cookie("JWT_TOKEN", loginResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(SevenDays);

            response.addCookie(jwtCookie);
            response.setHeader("X-JWT-TOKEN", loginResponse.getToken());
        }

        return ResponseEntity.status(loginResponse.getStatusCode()).body(loginResponse);
    }

    @PostMapping("/login-google")
    public ResponseEntity<Response> loginGoogle(
            @RequestParam("email") String email,
            @RequestParam("avatarPhotoUrl") String avatarPhotoUrl,
            @RequestParam("fullName") String fullName,
            HttpServletResponse response) {
        Response loginGoogleResponse = authApi.loginGoogle(email, avatarPhotoUrl, fullName);

        boolean isOk = loginGoogleResponse.getStatusCode() == 200;

        if (isOk) {
            int SevenDays = 7 * 24 * 60 * 60;
            Cookie jwtCookie = new Cookie("JWT_TOKEN", loginGoogleResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(SevenDays);

            response.addCookie(jwtCookie);
            response.setHeader("X-JWT-TOKEN", loginGoogleResponse.getToken());
        }

        return ResponseEntity.status(loginGoogleResponse.getStatusCode()).body(loginGoogleResponse);
    }

    @PostMapping("/send-otp/{email}")
    public ResponseEntity<Response> sendOTP(@PathVariable("email") String email) {
        Response response = authApi.sendOTP(email);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/check-otp/{email}")
    public ResponseEntity<Response> checkOTP(
            @PathVariable("email") String email,
            @RequestParam("otp") String otp) {
        Response response = authApi.checkOTP(email, otp);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/change-password/{userId}")
    public ResponseEntity<Response> changePassword(
            @PathVariable("userId") String userId,
            @RequestParam("oldPassword") String oldPassword,
            @RequestParam("newPassword") String newPassword) {
        Response response = authApi.changePassword(userId, oldPassword, newPassword);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/reset-password/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> resetPassword(
            @PathVariable("userId") String userId) {
        Response response = authApi.resetPassword(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/forgot-password/{email}")
    public ResponseEntity<Response> forgotPassword(
            @PathVariable("email") String email,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("rePassword") String rePassword) {
        Response response = authApi.forgotPassword(email, newPassword, rePassword);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletResponse response) {
        try {
            Cookie cookie = new Cookie("JWT_TOKEN", null);
            cookie.setHttpOnly(true);
            cookie.setSecure(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);

            Response responseLogout = new Response(200, "logged out successfully");
            return ResponseEntity.status(responseLogout.getStatusCode()).body(responseLogout);
        } catch (Exception e) {
            Response errorResponse = new Response(500, "Logout failed: " + e.getMessage());
            return ResponseEntity.status(errorResponse.getStatusCode()).body(errorResponse);
        }
    }

    @PostMapping("/check-admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> checkAdmin() {
        Response response = authApi.checkAdmin();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}