package com.Server.service.instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OnlineUserTracker {
    private static final Logger log = LoggerFactory.getLogger(OnlineUserTracker.class);
    private final Set<String> onlineUsers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to add null or empty userId to online users");
            return;
        }

        boolean wasAdded = onlineUsers.add(userId);
        if (wasAdded) {
            log.debug("User added to online users: {}, total online: {}", userId, onlineUsers.size());
        }
    }

    public void removeUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Attempted to remove null or empty userId from online users");
            return;
        }

        boolean wasRemoved = onlineUsers.remove(userId);
        if (wasRemoved) {
            log.debug("User removed from online users: {}, total online: {}", userId, onlineUsers.size());
        }
    }

    public boolean isUserOnline(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            log.warn("Checked online status for null or empty userId");
            return false;
        }
        return onlineUsers.contains(userId);
    }

    public Set<String> getOnlineUsers() {
        Set<String> onlineUsersCopy = new HashSet<>(onlineUsers);
        log.debug("Retrieved online users list, count: {}", onlineUsersCopy.size());
        return onlineUsersCopy;
    }

    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
}