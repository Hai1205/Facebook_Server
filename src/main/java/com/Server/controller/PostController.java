package com.Server.controller;

import com.Server.dto.Response;
import com.Server.service.api.PostsApi;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    @Autowired
    private PostsApi postsApi;

    @GetMapping("/")
    public ResponseEntity<Response> getAllPost() {
        Response response = postsApi.getAllPost();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-all-story")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllStory() {
        Response response = postsApi.getAllStory();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-all-report")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Response> getAllReport() {
        Response response = postsApi.getAllReport();

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    
    @GetMapping("/get-post/{postId}")
    public ResponseEntity<Response> getPost(@PathVariable("postId") String postId) {
        Response response = postsApi.getPost(postId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create-post/{userId}")
    public ResponseEntity<Response> createPost(
            @PathVariable("userId") String userId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam("content") String content,
            @RequestParam("privacy") String privacy) {
        Response response = postsApi.createPost(userId, files, content, privacy);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/create-story/{userId}")
    public ResponseEntity<Response> createStory(
            @PathVariable("userId") String userId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("privacy") String privacy) {
        Response response = postsApi.createStory(userId, file, privacy);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-post/{postId}")
    public ResponseEntity<Response> deletePost(@PathVariable("postId") String postId) {
        Response response = postsApi.deletePost(postId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-story/{storyId}")
    public ResponseEntity<Response> deleteStory(@PathVariable("storyId") String storyId) {
        Response response = postsApi.deleteStory(storyId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/like-post/{postId}/{userId}")
    public ResponseEntity<Response> likePost(
            @PathVariable("postId") String postId,
            @PathVariable("userId") String userId) {
        Response response = postsApi.likePost(postId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/comment-post/{postId}/{userId}")
    public ResponseEntity<Response> commentPost(
            @PathVariable("postId") String postId,
            @PathVariable("userId") String userId,
            @RequestParam("text") String text) {
        Response response = postsApi.commentPost(postId, userId, text);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-comment/{commentId}/{postId}")
    public ResponseEntity<Response> deleteComment(
            @PathVariable("commentId") String commentId,
            @PathVariable("postId") String postId) {
        Response response = postsApi.deleteComment(commentId, postId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/share-post/{postId}/{userId}")
    public ResponseEntity<Response> sharePost(
            @PathVariable("postId") String postId,
            @PathVariable("userId") String userId) {
        Response response = postsApi.sharePost(postId, userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-feed/{userId}")
    public ResponseEntity<Response> getUserFeed(
            @PathVariable(value = "userId", required = false) String userId) {
        Response response = postsApi.getUserFeed(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-feed/")
    public ResponseEntity<Response> getPublicFeed() {
        Response response = postsApi.getUserFeed(null);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/get-user-story-feed/{userId}")
    public ResponseEntity<Response> getUserStoryFeed(
            @PathVariable("userId") String userId) {
        Response response = postsApi.getUserStoryFeed(userId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update-post/{postId}")
    public ResponseEntity<Response> updatePost(
            @PathVariable("postId") String postId,
            @RequestParam(value = "file", required = false) List<MultipartFile> files,
            @RequestParam("content") String content,
            @RequestParam("privacy") String privacy) {
        Response response = postsApi.updatePost(postId, files, content, privacy);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search-posts")
    public ResponseEntity<Response> searchPosts(
            @RequestParam(name = "content", required = false) String content,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "privacy", required = false) String privacy) {
        Response response = postsApi.searchPosts(content, status, privacy);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/search-reports")
    public ResponseEntity<Response> searchReports(
            @RequestParam(name = "reason", required = false) String reason,
            @RequestParam(name = "contentType", required = false) String contentType,
            @RequestParam(name = "status", required = false) String status) {
        Response response = postsApi.searchReports(contentType, reason, status);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/report/{userId}/{contentId}")
    public ResponseEntity<Response> report(
            @PathVariable("userId") String userId,
            @PathVariable("contentId") String contentId,
            @RequestParam("contentType") String contentType,
            @RequestParam("reason") String reason,
            @RequestParam("additionalInfo") String additionalInfo) {
        Response response = postsApi.report(contentId, userId, contentType, reason, additionalInfo);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/resolve-report/{contentId}")
    public ResponseEntity<Response> resolveReport(
            @PathVariable("contentId") String contentId,
            @RequestParam("status") String status) {
        Response response = postsApi.resolveReport(contentId, status);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @DeleteMapping("/delete-report/{reportId}")
    public ResponseEntity<Response> deleteReport(@PathVariable("reportId") String reportId) {
        Response response = postsApi.deleteReport(reportId);

        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}