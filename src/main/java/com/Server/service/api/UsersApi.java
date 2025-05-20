package com.Server.service.api;

import com.Server.dto.*;
import com.Server.entity.Noti;
import com.Server.exception.OurException;
import com.Server.repo.NotiRepository;
import com.Server.service.config.AwsS3Config;
import com.Server.utils.mapper.FriendRequestMapper;
import com.Server.utils.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.Server.entity.Bio;
import com.Server.entity.FriendRequest;
import com.Server.entity.User;
import com.Server.repo.BioRepository;
import com.Server.repo.FriendRequestRepository;
import com.Server.repo.UserRepository;

import org.springframework.web.multipart.MultipartFile;

@Service
public class UsersApi {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotiRepository notiRepository;

    @Autowired
    private AwsS3Config awsS3Config;

    @Autowired
    private BioRepository bioRepository;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    UsersApi(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Response getAllUser(int page, int limit, String sort, String order) {
        Response response = new Response();

        try {
            Sort.Direction direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
            Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

            Page<User> userPage = userRepository.findAll(pageable);
            List<UserDTO> userDTOList = UserMapper.mapListEntityToListDTOFull(userPage.getContent());

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setPagination(new Pagination(userPage.getTotalElements(), userPage.getTotalPages(), page));
            response.setUsers(userDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    private String getFriendStatus(User currentUser, User targetUser) {
        if (isFriend(currentUser, targetUser.getId())) {
            return "FRIEND";
        }

        List<FriendRequest> requests = friendRequestRepository.findRequestsBetweenUsers(
                currentUser.getId(), targetUser.getId());

        if (requests == null || requests.isEmpty()) {
            return "NONE";
        }

        FriendRequest request = requests.get(0);

        boolean isSent = request.getFrom().getId().equals(currentUser.getId());
        if (isSent) {
            return "SENT";
        } else {
            return "PENDING";
        }
    }

    public Response getUser(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(user);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUser(userDTO);
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

    public Response getUserProfile(String currentUserId, String targetUserId) {
        Response response = new Response();

        try {
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new OurException("Target user Not Found"));
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(targetUser);

            if (!currentUserId.equals("NONE")) {
                User currentUser = userRepository.findById(currentUserId)
                        .orElseThrow(() -> new OurException("Current user Not Found"));

                String friendStatus = getFriendStatus(currentUser, targetUser);
                response.setFriendStatus(friendStatus);
            }

            response.setStatusCode(200);
            response.setMessage("get user profile successfully");
            response.setUser(userDTO);
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

    public Response getUserSuggested(String userId) {
        Response response = new Response();
        try {
            User currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new OurException("User Not Found"));

            List<String> followingIds = currentUser.getFollowing().stream()
                    .map(User::getId)
                    .toList();

            List<String> friendIds = currentUser.getFriends().stream()
                    .map(User::getId)
                    .toList();

            List<User> temp = userRepository.findAll().stream()
                    .filter(user -> !user.getId().equals(userId))
                    .filter(user -> !followingIds.contains(user.getId()))
                    .filter(user -> !friendIds.contains(user.getId()))
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                Collections.shuffle(list);
                                return list.stream().limit(5).collect(Collectors.toList());
                            }));

            List<UserDTO> userDTOList = UserMapper.mapListEntityToListDTOFull(temp);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUsers(userDTOList);
        } catch (OurException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Internal Server Error");
            e.printStackTrace();
        }
        return response;
    }

    public Response followUser(String currentUserId, String opponentId) {
        Response response = new Response();

        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new OurException("Current User Not Found"));
            User opponent = userRepository.findById(opponentId)
                    .orElseThrow(() -> new OurException("Opponent User Not Found"));

            if (opponentId.equals(currentUserId)) {
                response.setStatusCode(400);
                response.setMessage("You can't follow/unfollow yourself");

                return response;
            }

            boolean isFollowing = isFollow(currentUser, opponentId);

            if (isFollowing) {
                currentUser.getFollowing().removeIf(dbRef -> dbRef.getId().toString().equals(opponentId));
                opponent.getFollowers().removeIf(dbRef -> dbRef.getId().toString().equals(currentUserId));

                response.setMessage("User unfollowed successfully");
            } else {
                currentUser.getFollowing().add(opponent);
                opponent.getFollowers().add(currentUser);

                Noti notification = new Noti(Noti.TYPE.FOLLOW, currentUser, opponent);
                notiRepository.save(notification);

                response.setMessage("User followed successfully");
            }

            userRepository.save(currentUser);
            userRepository.save(opponent);

            UserDTO userDTO = UserMapper.mapEntityToDTOFull(currentUser);

            response.setStatusCode(200);
            response.setUser(userDTO);
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

    private boolean isFollow(User currentUser, String opponentId) {
        return currentUser.getFollowing()
                .stream()
                .map(dbRef -> dbRef.getId().toString())
                .anyMatch(id -> id.equals(opponentId));
    }

    public Response sendFriendRequest(String currentUserId, String opponentId) {
        Response response = new Response();

        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new OurException("Current User Not Found"));
            User opponent = userRepository.findById(opponentId)
                    .orElseThrow(() -> new OurException("Opponent User Not Found"));

            if (opponentId.equals(currentUserId)) {
                response.setStatusCode(400);
                response.setMessage("You can't send a friend request to yourself");
                return response;
            }

            boolean isFollowing = isFollow(currentUser, opponentId);
            if (!isFollowing) {
                currentUser.getFollowing().add(opponent);
                opponent.getFollowers().add(currentUser);
            }
            userRepository.save(currentUser);
            userRepository.save(opponent);

            FriendRequest existingRequest = friendRequestRepository.findByFromIdAndToId(currentUser.getId(),
                    opponent.getId());
            if (existingRequest != null) {
                friendRequestRepository.delete(existingRequest);
                response.setMessage("Friend request canceled successfully");
            } else {
                FriendRequest newRequest = new FriendRequest(currentUser, opponent);
                newRequest.setStatus(FriendRequest.STATUS.PENDING);
                friendRequestRepository.save(newRequest);
                response.setMessage("Friend request sent successfully");

                FriendRequestDTO friendRequestDTO = FriendRequestMapper.mapEntityToDTOFull(newRequest);
                response.setFriendRequest(friendRequestDTO);
            }

            response.setStatusCode(200);
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

    private boolean isFriend(User currentUser, String opponentId) {
        return currentUser.getFriends()
                .stream()
                .map(dbRef -> dbRef.getId().toString())
                .anyMatch(id -> id.equals(opponentId));
    }

    public Response unfriend(String currentUserId, String opponentId) {
        Response response = new Response();

        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new OurException("Current User Not Found"));
            User opponent = userRepository.findById(opponentId)
                    .orElseThrow(() -> new OurException("Opponent User Not Found"));

            if (opponentId.equals(currentUserId)) {
                throw new OurException("You can't unfriend to yourself");
            }

            boolean isFriend = isFriend(currentUser, opponentId);
            if (!isFriend) {
                throw new OurException("You are not a friend with this user");
            }

            boolean isCurrentUserFollowing = isFollow(currentUser, opponentId);
            if (isCurrentUserFollowing) {
                currentUser.getFollowing().remove(opponent);
                opponent.getFollowers().remove(currentUser);
            }

            boolean isOpponentFollowing = isFollow(opponent, currentUserId);
            if (isOpponentFollowing) {
                opponent.getFollowing().remove(currentUser);
                currentUser.getFollowers().remove(opponent);
            }

            currentUser.getFriends().remove(opponent);
            opponent.getFriends().remove(currentUser);

            userRepository.save(currentUser);
            userRepository.save(opponent);

            response.setStatusCode(200);
            response.setMessage("Unfriend successfully");
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

    public Response respondFriendRequest(String currentUserId, String opponentId, String status) {
        Response response = new Response();

        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new OurException("Current User Not Found"));
            User opponent = userRepository.findById(opponentId)
                    .orElseThrow(() -> new OurException("Opponent User Not Found"));

            FriendRequest friendRequest = friendRequestRepository.findByFromIdAndToId(opponent.getId(),
                    currentUser.getId());

            if (friendRequest == null) {
                throw new OurException("No request found for this user");
            }

            if (status.equals("ACCEPT")) {
                currentUser.getFriends().add(opponent);
                opponent.getFriends().add(currentUser);
                userRepository.save(currentUser);
                userRepository.save(opponent);

                boolean isFollowing = isFollow(currentUser, opponentId);
                if (!isFollowing) {
                    currentUser.getFollowing().add(opponent);
                    opponent.getFollowers().add(currentUser);
                }
            }

            userRepository.save(currentUser);
            userRepository.save(opponent);

            friendRequestRepository.delete(friendRequest);

            response.setStatusCode(200);
            response.setMessage(String.format("Response Friend request from %s successfully", opponent.getFullName()));
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

    public Response getAllFriendRequest() {
        Response response = new Response();

        try {
            List<FriendRequest> friendRequest = friendRequestRepository.findAll();

            List<FriendRequestDTO> friendRequestDTOList = FriendRequestMapper.mapListEntityToListDTOFull(friendRequest);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setFriendRequests(friendRequestDTOList);
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

    public Response getUserFriendRequests(String userId) {
        Response response = new Response();

        try {
            List<FriendRequest> friendRequest = friendRequestRepository.findByTo(userId);
            List<FriendRequestDTO> friendRequestDTOList = FriendRequestMapper.mapListEntityToListDTOFull(friendRequest);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setFriendRequests(friendRequestDTOList);
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

    public Response updateUser(String userId,
            MultipartFile avatarPhoto,
            MultipartFile coverPhoto,
            String fullName,
            String role,
            String gender,
            String dateOfBirth,
            String status,
            boolean celebrity) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            if (avatarPhoto != null && !avatarPhoto.isEmpty()) {
                String avatarPhotoUrl = user.getAvatarPhotoUrl();
                if (avatarPhotoUrl != null &&
                        !avatarPhotoUrl.isEmpty())
                    awsS3Config.deleteFileFromS3(avatarPhotoUrl);

                avatarPhotoUrl = awsS3Config.saveFileToS3(avatarPhoto);
                user.setAvatarPhotoUrl(avatarPhotoUrl);
            }

            if (coverPhoto != null && !coverPhoto.isEmpty()) {
                String coverPhotoUrl = user.getCoverPhotoUrl();
                if (coverPhotoUrl != null &&
                        !coverPhotoUrl.isEmpty())
                    awsS3Config.deleteFileFromS3(coverPhotoUrl);

                coverPhotoUrl = awsS3Config.saveFileToS3(coverPhoto);
                user.setCoverPhotoUrl(coverPhotoUrl);
            }

            if (fullName != null && !fullName.isEmpty() && !fullName.equals(user.getFullName())) {
                user.setFullName(fullName);
            }

            if (dateOfBirth != null && !dateOfBirth.isEmpty() && !dateOfBirth.equals(user.getDateOfBirth().toString())) {
                LocalDate localDateOfBirth = LocalDate.parse(dateOfBirth);
                Date dateOfBirthObj = Date.valueOf(localDateOfBirth);
                user.setDateOfBirth(dateOfBirthObj);
            }

            if (role != null && !role.isEmpty() && !role.equals(user.getRole().toString())) {
                user.setRole(User.Role.valueOf(role));
            }

            if (gender != null && !gender.isEmpty() && !gender.equals(user.getGender().toString())) {
                user.setGender(User.Gender.valueOf(gender));
            }

            if (status != null && !status.isEmpty() && !status.equals(user.getStatus().toString())) {
                user.setStatus(User.Status.valueOf(status));
            }

            if (celebrity != user.isCelebrity()) {
                user.setCelebrity(celebrity);
            }

            User savedUser = userRepository.save(user);
            UserDTO userDTO = UserMapper.mapEntityToDTOFull(savedUser);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUser(userDTO);
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

    public Response deleteUser(String userId) {
        Response response = new Response();

        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new OurException("User Not Found"));

            String avatarUrl = user.getAvatarPhotoUrl();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                awsS3Config.deleteFileFromS3(avatarUrl);
            }

            String coverUrl = user.getCoverPhotoUrl();
            if (coverUrl != null && !coverUrl.isEmpty()) {
                awsS3Config.deleteFileFromS3(coverUrl);
            }

            userRepository.deleteById(userId);

            response.setStatusCode(200);
            response.setMessage("successful");
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

    public Response createUser(String email, String fullName, String password, String gender, String dateOfBirth, String role) {
        Response response = new Response();

        try {
            if (userRepository.existsByEmail(email)) {
                throw new OurException("Email Already Exists");
            }

            if (fullName == null || fullName.isEmpty()) {
                throw new OurException("Full name is required");
            }

            if (gender == null || gender.isEmpty()) {
                throw new OurException("Gender is required");
            }

            if (dateOfBirth == null || dateOfBirth.isEmpty()) {
                throw new OurException("Date of birth is required");
            }

            if (role == null || role.isEmpty()) {
                throw new OurException("Role is required");
            }

            User.Role userRole = User.Role.valueOf(role);

            LocalDate localDateOfBirth = LocalDate.parse(dateOfBirth);
            Date dateOfBirthObj = Date.valueOf(localDateOfBirth);

            User user = new User(email, fullName, User.Gender.valueOf(gender), dateOfBirthObj, userRole);
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

    public Response searchUsers(String query, String gender, String status, String role) {
        Response response = new Response();

        try {
            List<User> users = userRepository.findAll();

            if (gender != null && !gender.isEmpty()) {
                String[] genderValues = gender.split(",");
                users = users.stream()
                        .filter(user -> {
                            for (String genderValue : genderValues) {
                                if (user.getGender().toString().equals(genderValue.trim())) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }

            if (status != null && !status.isEmpty()) {
                String[] statusValues = status.split(",");
                users = users.stream()
                        .filter(user -> {
                            for (String statusValue : statusValues) {
                                if (user.getStatus().equals(User.Status.valueOf(statusValue.trim()))) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }

            if (role != null && !role.isEmpty()) {
                String[] roleValues = role.split(",");
                users = users.stream()
                        .filter(user -> {
                            for (String roleValue : roleValues) {
                                if (user.getRole().equals(User.Role.valueOf(roleValue.trim()))) {
                                    return true;
                                }
                            }
                            return false;
                        })
                        .collect(Collectors.toList());
            }

            if (query != null && !query.isEmpty()) {
                users = users.stream()
                        .filter(user -> (user.getEmail() != null && user.getEmail().contains(query)) ||
                                (user.getFullName() != null && user.getFullName().contains(query)))
                        .collect(Collectors.toList());
            }

            List<UserDTO> userDTOList = UserMapper.mapListEntityToListDTOFull(users);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setUsers(userDTOList);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
            System.out.println(e.getMessage());
        }

        return response;
    }

    public Response getFriendRequestStatus(String currentUserId, String targetUserId) {
        Response response = new Response();

        try {
            User currentUser = userRepository.findById(currentUserId)
                    .orElseThrow(() -> new OurException("Current User Not Found"));
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new OurException("Target User Not Found"));

            String friendStatus = getFriendStatus(currentUser, targetUser);

            response.setStatusCode(200);
            response.setMessage("Success");
            response.setFriendStatus(friendStatus);
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
