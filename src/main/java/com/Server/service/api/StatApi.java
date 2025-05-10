package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.repo.*;
import com.Server.exception.OurException;
import com.Server.utils.mapper.*;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
public class StatApi {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ReportRepository reportRepository;

    public Response getGeneralStat() {
        Response response = new Response();

        try {
            long totalUsers = userRepository.count();
            long totalPosts = postRepository.count();
            long totalComments = commentRepository.count();
            long totalReports = reportRepository.count();

            response.setStatusCode(200);
            response.setMessage("Get general statistics successfully");
            response.setGeneralStat(Map.of(
                    "totalPosts", totalPosts,
                    "totalComments", totalComments,
                    "totalUsers", totalUsers,
                    "totalReports", totalReports));
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getPopularPostStat() {
        Response response = new Response();

        try {
            int limit = 5;
            Pageable pageable = PageRequest.of(0, limit);
            List<Post> posts = postRepository.findTopPostsByLikes(pageable);

            List<PostDTO> postDTOList = PostMapper.mapListEntityToListDTOFull(posts);

            response.setStatusCode(200);
            response.setMessage("Get top post statistics successfully");
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

    public Response getTopUsersStat() {
        Response response = new Response();
        try {
            int limit = 5;
            Pageable pageable = PageRequest.of(0, limit);
            List<User> users = userRepository.findTopUsersByFollowers(pageable);
            
            List<UserDTO> userDTOList = UserMapper.mapListEntityToListDTOFull(users);

            response.setStatusCode(200);
            response.setMessage("Get top users statistics successfully");
            response.setUsers(userDTOList);
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
}
