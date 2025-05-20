package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.api.BioApi;
import com.Server.service.api.UsersApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UsersApi usersApi;

    @Autowired
    private BioApi bioApi;

    @PutMapping("/update-user-bio/{userId}")
    public ResponseEntity<Response> updateUserBio(
            @PathVariable("userId") String userId,
            @RequestParam("bioText") String bioText,
            @RequestParam("liveIn") String liveIn,
            @RequestParam("relationship") String relationship,
            @RequestParam("workplace") String workplace,
            @RequestParam("education") String education,
            @RequestParam("phone") String phone,
            @RequestParam("hometown") String hometown) {
        Response response = bioApi.updateUserBio(userId, bioText, liveIn, relationship, workplace, education, phone,
                hometown);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update-cover-photo/{userId}")
    public ResponseEntity<Response> updateCoverPhoto(
            @PathVariable("userId") String userId,
            @RequestParam(value = "coverPhoto", required = true) MultipartFile coverPhoto) {
        Response response = bioApi.updateCoverPhoto(userId, coverPhoto);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update-avatar-photo/{userId}")
    public ResponseEntity<Response> updateAvatarPhoto(
            @PathVariable("userId") String userId,
            @RequestParam(value = "avatarPhoto", required = true) MultipartFile avatarPhoto) {
        Response response = bioApi.updateAvatarPhoto(userId, avatarPhoto);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllUser(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "limit", defaultValue = "10") int limit,
            @RequestParam(name = "sort", defaultValue = "createdAt") String sort,
            @RequestParam(name = "order", defaultValue = "asc") String order) {
        Response response = usersApi.getAllUser(page, limit, sort, order);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user/{userId}")
    public ResponseEntity<Response> getUser(@PathVariable("userId") String userId) {
        Response response = usersApi.getUser(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-profile/{currentUserId}/{targetUserId}")
    public ResponseEntity<Response> getUser(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("targetUserId") String targetUserId) {
        Response response = usersApi.getUserProfile(currentUserId, targetUserId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create-user")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> createUser(
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestParam("password") String password,
            @RequestParam("gender") String gender,
            @RequestParam("dateOfBirth") String dateOfBirth,
            @RequestParam("role") String role) {
        Response response = usersApi.createUser(email, fullName, password, gender, dateOfBirth, role);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-suggested/{userId}")
    public ResponseEntity<Response> getUserSuggested(@PathVariable("userId") String userId) {
        Response response = usersApi.getUserSuggested(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/follow-user/{currentUserId}/{opponentId}")
    public ResponseEntity<Response> followUser(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("opponentId") String opponentId) {
        Response response = usersApi.followUser(currentUserId, opponentId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/send-friend-request/{currentUserId}/{opponentId}")
    public ResponseEntity<Response> sendFriendRequest(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("opponentId") String opponentId) {
        Response response = usersApi.sendFriendRequest(currentUserId, opponentId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/unfriend/{currentUserId}/{opponentId}")
    public ResponseEntity<Response> unfriend(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("opponentId") String opponentId) {
        Response response = usersApi.unfriend(currentUserId, opponentId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/respond-friend-request/{currentUserId}/{opponentId}")
    public ResponseEntity<Response> respondFriendRequest(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("opponentId") String opponentId,
            @RequestParam("status") String status) {
        Response response = usersApi.respondFriendRequest(currentUserId, opponentId, status);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-all-friend-request")
    public ResponseEntity<Response> getAllFriendsRequest() {
        Response response = usersApi.getAllFriendRequest();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-friend-request/{userId}")
    public ResponseEntity<Response> getUserFriendsRequests(
            @PathVariable("userId") String userId) {
        Response response = usersApi.getUserFriendRequests(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update-user/{userId}")
    public ResponseEntity<Response> updateUser(
            @PathVariable("userId") String userId,
            @RequestParam(value = "avatarPhoto", required = false) MultipartFile avatarPhoto,
            @RequestParam(value = "coverPhoto", required = false) MultipartFile coverPhoto,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "dateOfBirth", required = false) String dateOfBirth,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "celebrity", required = false) boolean celebrity) {
        Response response = usersApi.updateUser(userId, avatarPhoto, coverPhoto, fullName, role, gender, dateOfBirth,
                status, celebrity);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-user/{userId}")
    public ResponseEntity<Response> deleteUser(@PathVariable("userId") String userId) {
        Response response = usersApi.deleteUser(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search-users")
    public ResponseEntity<Response> searchUsers(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "gender", required = false) String gender,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "role", required = false) String role) {
        Response response = usersApi.searchUsers(query, gender, status, role);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/friend-request-status/{currentUserId}/{targetUserId}")
    public ResponseEntity<Response> getFriendRequestStatus(
            @PathVariable("currentUserId") String currentUserId,
            @PathVariable("targetUserId") String targetUserId) {
        Response response = usersApi.getFriendRequestStatus(currentUserId, targetUserId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
