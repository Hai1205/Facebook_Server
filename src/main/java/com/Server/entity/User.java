package com.Server.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Document(collection = "users")
@JsonIgnoreProperties({ "password" })
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    public User(String email, Gender gender, Date dateOfBirth, String fullName) {
        this.username = getUsernameByEmail(email);
        this.email = email;
        this.gender = gender;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
    }

    public User(String fullName, String email, Gender gender, String avatarUrl, Status status) {
        this.username = getUsernameByEmail(email);
        this.email = email;
        this.fullName = fullName;
        this.gender = gender;
        this.avatarPhotoUrl = avatarUrl;
        this.status = status;
    }

    public User(String fullName, String email, String avatarUrl, Status status) {
        this.username = getUsernameByEmail(email);
        this.email = email;
        this.fullName = fullName;
        this.avatarPhotoUrl = avatarUrl;
        this.status = status;
    }

    public User(String email, String fullName, Gender gender, Date dateOfBirth, Role role) {
        this.username = getUsernameByEmail(email);
        this.fullName = fullName;
        this.email = email;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.role = role;
    }

    @Id
    private String id;

    private String username;

    @Email(message = "Email is invalid")
    private String email;

    // @Size(min = 6, message = "Password must at least 6 characters")
    private String password;

    @Field(targetType = FieldType.STRING)
    private Gender gender = Gender.OTHER;

    private String fullName;

    private Date dateOfBirth;

    private String avatarPhotoUrl;

    private String coverPhotoUrl;

    private int reportCount = 0;

    private boolean isCelebrity = false;

    @DBRef
    private List<User> followers = new ArrayList<>();

    @DBRef
    private List<User> following = new ArrayList<>();

    @DBRef
    private List<Post> posts = new ArrayList<>();

    @DBRef
    private List<Story> stories = new ArrayList<>();

    @DBRef
    private List<User> friends = new ArrayList<>();

    @DBRef
    private Bio bio;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Field(targetType = FieldType.STRING)
    private Role role = Role.USER;

    @Field(targetType = FieldType.STRING)
    private Status status = Status.PENDING;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (role != null) {
            authorities.add(new SimpleGrantedAuthority(role.toString()));
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", fullName='" + fullName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", avatarPhotoUrl='" + avatarPhotoUrl + '\'' +
                ", coverPhotoUrl='" + coverPhotoUrl + '\'' +
                ", followersCount=" + (followers != null ? followers.size() : 0) + '\'' +
                ", followingCount=" + (following != null ? following.size() : 0) + '\'' +
                ", postsCount=" + (posts != null ? posts.size() : 0) + '\'' +
                ", storiesCount=" + (stories != null ? stories.size() : 0) + '\'' +
                ", friendsCount=" + (friends != null ? friends.size() : 0) + '\'' +
                ", role=" + role + '\'' +
                ", isCelebrity=" + isCelebrity + '\'' +
                ", createdAt=" + createdAt + '\'' +
                ", updatedAt=" + updatedAt +
                '}';
    }

    private String getUsernameByEmail(String email) {
        int atIndex = email.indexOf('@');

        String username = "";
        if (atIndex != -1) {
            username = email.substring(0, atIndex);
        }

        return username;
    }

    public enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    public enum Role {
        ADMIN,
        USER
    }

    public enum Status {
        PENDING,
        LOCK,
        ACTIVE
    }

    public enum FriendStatus {
        PENDING,
        NONE,
        SENT,
        FRIEND
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}