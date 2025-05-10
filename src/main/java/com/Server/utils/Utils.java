package com.Server.utils;

import com.Server.dto.*;
import com.Server.entity.User;
import com.Server.exception.OurException;
import com.Server.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;

public class Utils {
    private static final String NUMERIC_STRING = "0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateOTP(int length) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(NUMERIC_STRING.length());
            char randomChar = NUMERIC_STRING.charAt(randomIndex);
            stringBuilder.append(randomChar);
        }

        return stringBuilder.toString();
    }

    public static String generatePassword(int length) {
        return UUID.randomUUID().toString().substring(0, length);
    }

    public static Request parseStringToJSonData(String formData) {
        try {
            return new ObjectMapper().readValue(formData, Request.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetailsService) {
            return ((CustomUserDetailsService) principal).getId();
        } else if (principal instanceof User) {
            return ((User) principal).getId();
        } else {
            throw new OurException("User is invalid");
        }
    }

    public static Object getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}