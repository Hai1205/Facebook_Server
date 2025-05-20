package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.*;
import com.Server.exception.OurException;
import com.Server.repo.*;
import com.Server.service.config.AwsS3Config;
import com.Server.utils.mapper.UserMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class BioApi {
    @Autowired
    private BioRepository bioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AwsS3Config awsS3Config;

    public Response updateUserBio(String userId, String bioText, String liveIn, String relationship, String workplace,
            String education, String phone, String hometown) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));
            Bio bio = bioRepository.findById(user.getBio().getId()).orElseThrow(() -> new OurException("User bio Not Found"));

            if (bioText != null && !bioText.isEmpty() && !bioText.equals(bio.getBioText())) {   
                bio.setBioText(bioText);
            }

            if (liveIn != null && !liveIn.isEmpty() && !liveIn.equals(bio.getLiveIn())) {
                bio.setLiveIn(liveIn);
            }

            if (relationship != null && !relationship.isEmpty() && !relationship.equals(bio.getRelationship())) {
                bio.setRelationship(relationship);
            }

            if (workplace != null && !workplace.isEmpty() && !workplace.equals(bio.getWorkplace())) {
                bio.setWorkplace(workplace);
            }

            if (education != null && !education.isEmpty() && !education.equals(bio.getEducation())) {
                bio.setEducation(education);
            }

            if (phone != null && !phone.isEmpty() && !phone.equals(bio.getPhone())) {
                bio.setPhone(phone);
            }

            if (hometown != null && !hometown.isEmpty() && !hometown.equals(bio.getHometown())) {
                bio.setHometown(hometown);
            }

            bioRepository.save(bio);
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(user);

            response.setStatusCode(200);
            response.setMessage("Bio updated successfully");
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

    public Response updateCoverPhoto(String userId, MultipartFile coverPhoto) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            String coverPhotoUrl = awsS3Config.saveFileToS3(coverPhoto);
            user.setCoverPhotoUrl(coverPhotoUrl);

            User savedUser = userRepository.save(user);
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(savedUser);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUser(userDTO);
            response.setExpirationTime("7 days");
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

    public Response updateAvatarPhoto(String userId, MultipartFile avatarPhoto) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            String avatarPhotoUrl = awsS3Config.saveFileToS3(avatarPhoto);
            user.setAvatarPhotoUrl(avatarPhotoUrl);

            User savedUser = userRepository.save(user);
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(savedUser);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUser(userDTO);
            response.setExpirationTime("7 days");
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
}
