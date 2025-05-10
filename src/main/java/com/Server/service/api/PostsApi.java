package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.exception.OurException;
import com.Server.repo.*;
import com.Server.service.config.AwsS3Config;
import com.Server.utils.mapper.PostMapper;
import com.Server.utils.mapper.ReportMapper;
import com.Server.utils.mapper.StoryMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

@Service
public class PostsApi {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private NotiRepository notiRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AwsS3Config awsS3Config;

    public Response createPost(String userId, MultipartFile file, String content, String privacy) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            Post post = new Post();
            post.setUser(user);
            post.setContent(content);
            post.setPrivacy(Post.Privacy.valueOf(privacy));

            if (file != null && !file.isEmpty()) {
                String mediaUrl = awsS3Config.saveFileToS3(file);
                Post.MediaType mediaType = file.getContentType().startsWith("video") ? Post.MediaType.VIDEO
                        : Post.MediaType.IMAGE;
                post.setMediaUrl(mediaUrl);
                post.setMediaType(mediaType);
            }

            Post savedPost = postRepository.save(post);

            user.getPosts().add(savedPost);
            userRepository.save(user);

            PostDTO postDTO = PostMapper.mapEntityToDTOFull(savedPost);

            response.setStatusCode(201);
            response.setMessage("Post created successfully");
            response.setPost(postDTO);
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

    public Response createStory(String userId, MultipartFile file, String privacy) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            Story story = new Story();
            story.setUser(user);

            if (file != null && !file.isEmpty()) {
                String mediaUrl = awsS3Config.saveFileToS3(file);
                Post.MediaType mediaType = file.getContentType().startsWith("video") ? Post.MediaType.VIDEO
                        : Post.MediaType.IMAGE;
                story.setMediaUrl(mediaUrl);
                story.setMediaType(mediaType);
            }

            story.setPrivacy(Post.Privacy.valueOf(privacy));

            Story savedStory = storyRepository.save(story);

            user.getStories().add(savedStory);
            userRepository.save(user);

            StoryDTO storyDTO = StoryMapper.mapEntityToDTOFull(savedStory);

            response.setStatusCode(201);
            response.setMessage("Story created successfully");
            response.setStory(storyDTO);
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

    public Response getAllStory() {
        Response response = new Response();

        try {
            List<Story> stories = storyRepository.findAllByOrderByCreatedAtDesc();
            List<StoryDTO> storyDTOList = StoryMapper.mapListEntityToListDTOFull(stories);

            response.setStatusCode(200);
            response.setMessage("Get all story successfully");
            response.setStories(storyDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getAllReport() {
        Response response = new Response();

        try {
            List<Report> reports = reportRepository.findAllByOrderByCreatedAtDesc();
            List<ReportDTO> reportDTOList = ReportMapper.mapListEntityToListDTO(reports);

            response.setStatusCode(200);
            response.setMessage("Get all report successfully");
            response.setReports(reportDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getAllPost() {
        Response response = new Response();

        try {
            List<Post> posts = postRepository.findAllByOrderByCreatedAtDesc();
            List<PostDTO> postDTOList = PostMapper.mapListEntityToListDTOFull(posts);

            response.setStatusCode(200);
            response.setMessage("Get all post successfully");
            response.setPosts(postDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getUserFeed(String userId) {
        Response response = new Response();

        try {
            List<Post> resultPosts = new ArrayList<>();

            if (userId == null || userId.isEmpty()) {
                resultPosts = postRepository.findAllByOrderByCreatedAtDesc();

                resultPosts = resultPosts.stream()
                        .filter(post -> !post.getPrivacy().equals(Post.Privacy.PRIVATE))
                        .collect(Collectors.toList());
            } else {
                User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

                Set<String> relevantUserIds = new HashSet<>();
                relevantUserIds.addAll(user.getFollowing().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()));
                relevantUserIds.addAll(user.getFriends().stream()
                        .map(User::getId)
                        .collect(Collectors.toSet()));

                relevantUserIds.add(userId);

                if (!relevantUserIds.isEmpty()) {
                    List<Post> relevantPosts = postRepository.findByUserIdInOrderByCreatedAtDesc(relevantUserIds);
                    Set<String> friendIds = user.getFriends().stream()
                            .map(User::getId)
                            .collect(Collectors.toSet());

                    relevantPosts = relevantPosts.stream()
                            .filter(post -> !post.getPrivacy().equals(Post.Privacy.PRIVATE) ||
                                    post.getUser().getId().equals(userId) ||
                                    friendIds.contains(post.getUser().getId()))
                            .collect(Collectors.toList());
                    resultPosts.addAll(relevantPosts);
                }

                int MAX_POSTS = 20;
                if (resultPosts.size() < MAX_POSTS) {
                    List<Post> otherPosts = postRepository.findAllByOrderByCreatedAtDesc();
                    Set<String> existingPostIds = resultPosts.stream()
                            .map(Post::getId)
                            .collect(Collectors.toSet());

                    otherPosts = otherPosts.stream()
                            .filter(post -> !post.getPrivacy().equals(Post.Privacy.PRIVATE))
                            .filter(post -> !existingPostIds.contains(post.getId()))
                            .collect(Collectors.toList());

                    otherPosts.stream()
                            .limit(MAX_POSTS - resultPosts.size())
                            .forEach(resultPosts::add);
                }
            }

            List<PostDTO> postDTOList = PostMapper.mapListEntityToListDTOFull(resultPosts);

            response.setStatusCode(200);
            response.setMessage("Get user feed successfully");
            response.setPosts(postDTOList);
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

    public Response getUserStoryFeed(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            List<String> followingIds = user.getFollowing().stream()
                    .map(User::getId)
                    .toList();
            List<String> friendIds = user.getFriends().stream()
                    .map(User::getId)
                    .toList();

            List<String> relatedUserIds = new ArrayList<>();
            relatedUserIds.addAll(followingIds);
            relatedUserIds.addAll(friendIds);

            List<Story> filteredStories = new ArrayList<>();

            if(!relatedUserIds.isEmpty()){
                filteredStories = storyRepository.findStoriesByUserIds(relatedUserIds);
            }

            List<StoryDTO> storyDTOList = StoryMapper.mapListEntityToListDTOFull(filteredStories);

            response.setStatusCode(200);
            response.setMessage("Get user story feed successfully");
            response.setStories(storyDTOList);
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

    public Response deletePost(String postId) {
        Response response = new Response();

        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new OurException("Post Not Found"));

            String imageUrl = post.getMediaUrl();
            awsS3Config.deleteFileFromS3(imageUrl);

            postRepository.deleteById(postId);

            response.setStatusCode(200);
            response.setMessage("success");
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

    public Response deleteStory(String storyId) {
        Response response = new Response();

        try {
            Story story = storyRepository.findById(storyId).orElseThrow(() -> new OurException("Story Not Found"));

            String imageUrl = story.getMediaUrl();
            awsS3Config.deleteFileFromS3(imageUrl);

            storyRepository.deleteById(storyId);

            response.setStatusCode(200);
            response.setMessage("success");
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

    public Response likePost(String postId, String userId) {
        Response response = new Response();

        try {
            System.out.println("postId: " + postId);
            System.out.println("userId: " + userId);
            Post post = postRepository.findById(postId).orElseThrow(() -> new OurException("Post not found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User not found"));

            boolean hasLiked = post.getLikes().contains(user);

            if (hasLiked) {
                post.getLikes().remove(user);
            } else {
                post.getLikes().add(user);

                Noti notification = new Noti(Noti.TYPE.LIKE, user, post.getUser());
                notiRepository.save(notification);
            }

            Post updatedPost = postRepository.save(post);
            PostDTO postDTO = PostMapper.mapEntityToDTOFull(updatedPost);

            response.setStatusCode(200);
            response.setMessage(hasLiked ? "Post unliked successfully" : "Post liked successfully");
            response.setPost(postDTO);
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

    public Response commentPost(String postId, String userId, String text) {
        Response response = new Response();

        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new OurException("Post not found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User not found"));

            Comment comment = new Comment();
            comment.setUser(user);
            comment.setText(text);
            Comment saveComment = commentRepository.save(comment);

            post.getComments().add(saveComment);

            Noti notification = new Noti(Noti.TYPE.COMMENT, user, post.getUser());
            notiRepository.save(notification);

            Post updatedPost = postRepository.save(post);
            PostDTO postDTO = PostMapper.mapEntityToDTOFull(updatedPost);

            response.setStatusCode(200);
            response.setMessage("Comment added successfully");
            response.setPost(postDTO);
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

    public Response sharePost(String postId, String userId) {
        Response response = new Response();

        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new OurException("Post Not Found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            post.getShares().add(user);

            Post updatedPost = postRepository.save(post);
            PostDTO postDTO = PostMapper.mapEntityToDTOFull(updatedPost);

            response.setStatusCode(201);
            response.setMessage("Post shared successfully");
            response.setPost(postDTO);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Internal Server Error");
        }

        return response;
    }

    public Response updatePost(String postId, MultipartFile file, String content, String privacy) {
        Response response = new Response();

        try {
            Post post = postRepository.findById(postId).orElseThrow(() -> new OurException("Post not found"));

            if (file != null && !file.isEmpty()) {
                String mediaUrl = post.getMediaUrl();
                if (mediaUrl != null &&
                        !mediaUrl.isEmpty())
                    awsS3Config.deleteFileFromS3(mediaUrl);

                mediaUrl = awsS3Config.saveFileToS3(file);
                post.setMediaUrl(mediaUrl);
            }

            post.setContent(content);
            post.setPrivacy(Post.Privacy.valueOf(privacy));

            Post updatedPost = postRepository.save(post);
            PostDTO postDTO = PostMapper.mapEntityToDTOFull(updatedPost);

            response.setStatusCode(200);
            response.setMessage("Post updated successfully");
            response.setPost(postDTO);
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

    public Response searchPosts(String content, String mediaType, String status, String privacy) {
        Response response = new Response();

        try {
            List<Post> posts = postRepository.findAll();

            if (privacy != null && !privacy.isEmpty()) {
                posts = posts.stream()
                        .filter(post -> post.getPrivacy().toString().equals(privacy))
                        .collect(Collectors.toList());
            }

            if (mediaType != null && !mediaType.isEmpty()) {
                posts = posts.stream()
                        .filter(user -> user.getMediaType().toString().equals(mediaType))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.isEmpty()) {
                posts = posts.stream()
                        .filter(user -> user.getStatus().toString().equals(status))
                        .collect(Collectors.toList());
            }

            if (content != null && !content.isEmpty()) {
                posts = posts.stream()
                        .filter(post -> post.getContent().contains(content))
                        .collect(Collectors.toList());
            }

            List<PostDTO> userDTOList = PostMapper.mapListEntityToListDTOFull(posts);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPosts(userDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response searchReports(String contentType, String reason, String status) {
        Response response = new Response();

        try {
            List<Report> reports = reportRepository.findAll();

            if (contentType != null && !contentType.isEmpty()) {
                reports = reports.stream()
                        .filter(report -> report.getContentType().toString().equals(contentType))
                        .collect(Collectors.toList());
            }

            if (status != null && !status.isEmpty()) {
                reports = reports.stream()
                        .filter(report -> report.getStatus().toString().equals(status))
                        .collect(Collectors.toList());
            }

            if (reason != null && !reason.isEmpty()) {
                reports = reports.stream()
                        .filter(report -> report.getReason().contains(reason))
                        .collect(Collectors.toList());
            }

            List<ReportDTO> reportDTOList = ReportMapper.mapListEntityToListDTOFull(reports);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setReports(reportDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    private boolean checkContent(String contentId, String contentType) {
        if (contentType.equals("POST")) {
            return postRepository.findById(contentId).isPresent();
        }

        if (contentType.equals("STORY")) {
            return storyRepository.findById(contentId).isPresent();
        }

        if (contentType.equals("COMMENT")) {
            return commentRepository.findById(contentId).isPresent();
        }

        if (contentType.equals("USER")) {
            return userRepository.findById(contentId).isPresent();
        }

        return false;
    }

    public Response report(String contentId, String userId, String contentType, String reason, String additionalInfo) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User not found"));

            if (!checkContent(contentId, contentType)) {
                throw new OurException("Content not found");
            }

            Report report = new Report();
            report.setSender(user);
            report.setContentId(contentId);
            report.setContentType(Report.ContentType.valueOf(contentType));
            report.setReason(reason);
            reportRepository.save(report);

            Report savedReport = reportRepository.save(report);
            ReportDTO reportDTO = ReportMapper.mapEntityToDTOFull(savedReport);

            response.setStatusCode(200);
            response.setMessage("Report created successfully");
            response.setReport(reportDTO);
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

    public Response resolveReport(String reportId, String status) {
        Response response = new Response();

        try {
            Report report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new OurException("Report not found"));

            if (!checkContent(report.getContentId(), report.getContentType() + "")) {
                throw new OurException("Content not found");
            }

            if (!report.getStatus().toString().equals("PENDING")) {
                throw new OurException("This report is already resolved");
            }

            Report.Status reportStatus = Report.Status.valueOf(status);

            if (reportStatus.equals(Report.Status.ACCEPT)) {
                incrementReportCount(report);
            }

            report.setStatus(reportStatus);
            reportRepository.save(report);

            Report savedReport = reportRepository.save(report);
            ReportDTO reportDTO = ReportMapper.mapEntityToDTOFull(savedReport);

            response.setStatusCode(200);
            response.setMessage("Report created successfully");
            response.setReport(reportDTO);
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

    private void incrementReportCount(Report report) throws OurException {
        Report.ContentType contentType = report.getContentType();
        switch (contentType) {
            case USER:
                User user = userRepository.findById(report.getSender().getId())
                        .orElseThrow(() -> new OurException("User not found"));
                user.setReportCount(user.getReportCount() + 1);
                if (user.getReportCount() == 3) {
                    user.setStatus(User.Status.LOCK);
                }
                userRepository.save(user);
                break;
            case POST:
                Post post = postRepository.findById(report.getContentId())
                        .orElseThrow(() -> new OurException("Post not found"));
                post.setReportCount(post.getReportCount() + 1);
                System.out.println(post.getReportCount());
                if (post.getReportCount() == 3) {
                    post.setStatus(User.Status.LOCK);
                }
                postRepository.save(post);
                break;
            case COMMENT:
                Comment comment = commentRepository.findById(report.getContentId())
                        .orElseThrow(() -> new OurException("Comment not found"));
                comment.setReportCount(comment.getReportCount() + 1);
                if (comment.getReportCount() == 3) {
                    comment.setStatus(User.Status.LOCK);
                }
                commentRepository.save(comment);
                break;
            case STORY:
                Story story = storyRepository.findById(report.getContentId())
                        .orElseThrow(() -> new OurException("Story not found"));
                story.setReportCount(story.getReportCount() + 1);
                if (story.getReportCount() == 3) {
                    story.setStatus(User.Status.LOCK);
                }
                storyRepository.save(story);
                break;
        }
    }

    public Response deleteReport(String reportId) {
        Response response = new Response();

        try {
            reportRepository.findById(reportId)
                    .orElseThrow(() -> new OurException("Report not found"));

            reportRepository.deleteById(reportId);

            response.setStatusCode(200);
            response.setMessage("Report deleted successfully");
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
}
