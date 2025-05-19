package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.repo.*;
import com.Server.exception.OurException;
import com.Server.service.config.MailConfig;
import com.Server.utils.JWTUtils;
import com.Server.utils.Utils;
import com.Server.utils.mapper.UserMapper;
import com.Server.handler.SocketIOHandler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AuthApi {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUtils jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private BioRepository bioRepository;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private SocketIOHandler socketIOHandler;

    public Response register(String email, String password, String gender, String fullName, String dateOfBirth) {
        Response response = new Response();

        try {
            if (userRepository.existsByEmail(email)) {
                throw new OurException("Email Already Exists");
            }

            if (gender == null || gender.isEmpty()) {
                throw new OurException("Gender is required");
            }

            if (dateOfBirth == null || dateOfBirth.isEmpty()) {
                throw new OurException("Date of birth is required");
            }

            LocalDate localDateOfBirth = LocalDate.parse(dateOfBirth);
            Date dateOfBirthObj = Date.from(localDateOfBirth.atStartOfDay(ZoneId.systemDefault()).toInstant());

            User user = new User(email, User.Gender.valueOf(gender), dateOfBirthObj, fullName);
            user.setPassword(passwordEncoder.encode(password));

            Bio bio = new Bio();
            bio = bioRepository.save(bio);
            user.setBio(bio);

            User savedUser = userRepository.save(user);
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(savedUser);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUser(userDTO);
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response login(String email, String password) {
        Response response = new Response();

        try {
            UserDetails userDetail = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            User user = userRepository.findByEmail(email).orElseThrow(() -> new OurException("User not found"));
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(user);

            String status = user.getStatus().toString();

            boolean isLock = status.equals("LOCK");
            if (isLock) {
                throw new OurException("Account has not been locked");
            }

            boolean isActive = status.equals("ACTIVE");
            if (isActive) {
                Authentication auth = new UsernamePasswordAuthenticationToken(userDetail.getUsername(), password);
                authenticationManager.authenticate(auth);

                var token = jwtUtils.generateToken(userDetail);

                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("email", user.getEmail());
                userInfo.put("fullName", user.getFullName());
                socketIOHandler.logUserLoginAndConnection(user.getId(), userInfo);

                response.setMessage("Login successfully");
                response.setToken(token);
            }

            response.setStatusCode(200);
            response.setUser(userDTO);
        } catch (BadCredentialsException e) {
            response.setStatusCode(401);
            response.setMessage(e.getMessage());
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response checkOTP(String email, String otp) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new OurException("User not found"));

            Otp otpInstance = otpRepository.findByCode(otp)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new OurException("OTP is invalid!"));

            if (otpInstance.getTimeExpired().isBefore(Instant.now())) {
                throw new OurException("OTP is expired!");
            }

            otpRepository.delete(otpInstance);
            user.setStatus(User.Status.ACTIVE);
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("Your account is activated!");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response sendOTP(String email) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new OurException("User not found"));

            String otp = Utils.generateOTP(6);

            Otp otpInstance = new Otp();
            otpInstance.setUser(user);
            otpInstance.setCode(otp);
            otpInstance.setTimeExpired(Instant.now().plus(10, ChronoUnit.MINUTES));
            otpRepository.save(otpInstance);

            String subject = "Account Activation";
            String templateName = "mail_active_account";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", user.getEmail());
            variables.put("recipientName", user.getEmail() != null ? user.getEmail() : "");
            variables.put("senderName", "Facebook");
            variables.put("otp", otp);

            mailConfig.sendMail(user.getEmail(), subject, templateName, variables);

            response.setStatusCode(200);
            response.setMessage("OTP is sent!");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response loginGoogle(String email, String avatarUrl, String fullName) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                user = new User(fullName, email, User.Gender.OTHER, avatarUrl, User.Status.ACTIVE);
                String randomPassword = Utils.generatePassword(10);
                user.setPassword(passwordEncoder.encode(randomPassword));

                Bio bio = new Bio();
                bio = bioRepository.save(bio);
                user.setBio(bio);

                user = userRepository.save(user);
            }

            UserDetails userDetails = userRepository.findByEmail(email)
                    .orElseThrow(() -> new OurException("User not found"));

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null,
                    userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            var token = jwtUtils.generateToken(userDetails);

            UserDTO userDTO = UserMapper.mapEntityToDTOFull(user);

            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("email", user.getEmail());
            userInfo.put("fullName", user.getFullName());
            socketIOHandler.logUserLoginAndConnection(user.getId(), userInfo);

            response.setStatusCode(200);
            response.setMessage("Login successfully");
            response.setToken(token);
            response.setUser(userDTO);
            response.setExpirationTime("7 days");
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response changePassword(String userId, String currentPassword, String newPassword, String rePassword) {
        Response response = new Response();

        try {
            if (!newPassword.equals(rePassword)) {
                throw new OurException("Password does not match.");
            }

            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User not found"));

            UserDetails userDetail = userRepository.findByUsername(user.getUsername())
                    .orElseThrow(() -> new OurException("User not found"));
            authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(userDetail.getUsername(), currentPassword));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("Password changed successfully!");
        } catch (BadCredentialsException e) {
            response.setStatusCode(401);
            response.setMessage("Old password is incorrect.");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response resetPassword(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User not found"));

            String password = Utils.generatePassword(10);
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);

            String subject = "Account Activation!";
            String templateName = "mail_active_account";
            Map<String, Object> variables = new HashMap<>();
            variables.put("recipientEmail", user.getEmail());
            variables.put("recipientName", user.getFullName());
            variables.put("senderName", "Facebook");
            variables.put("password", password);

            mailConfig.sendMail(user.getEmail(), subject, templateName, variables);

            response.setStatusCode(200);
            response.setMessage("Password reset email sent successfully!");
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response forgotPassword(String email, String newPassword, String rePassword) {
        Response response = new Response();

        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new OurException("User not found"));

            if (!newPassword.equals(rePassword)) {
                throw new OurException("Password does not match.");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("Password updated successfully!");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response checkAdmin() {
        Response response = new Response();

        try {
            response.setStatusCode(200);
            response.setMessage("You are admin!");
        } catch (OurException e) {
            response.setStatusCode(400);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("You are not admin!");
            System.out.println(e.getMessage());
        }

        return response;
    }
}
