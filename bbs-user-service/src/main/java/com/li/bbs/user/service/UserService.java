package com.li.bbs.user.service;

import com.li.bbs.user.domain.User;
import com.li.bbs.user.repository.UserMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public UserService(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Cacheable(value = "bbs:users", key = "#id")
    public Map<String, Object> getProfile(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Map.of("id", id, "displayName", "user-" + id);
        }
        return buildResponse(user.getId(), user.getDisplayName(), user.getProfile());
    }

    @CacheEvict(value = "bbs:users", key = "#id")
    public Map<String, Object> updateProfile(Long id, Map<String, Object> profile) {
        Map<String, Object> payload = profile == null ? Map.of() : profile;
        try {
            User existing = userMapper.selectById(id);
            String displayName = resolveDisplayName(payload, existing, id);
            String profileJson = objectMapper.writeValueAsString(payload);

            int updated = userMapper.updateProfile(id, displayName, profileJson);
            if (updated == 0) {
                User user = new User();
                user.setId(id);
                user.setDisplayName(displayName);
                user.setProfile(profileJson);
                userMapper.insert(user);
            }

            return buildResponse(id, displayName, profileJson);
        } catch (Exception e) {
            throw new RuntimeException("failed to update profile: " + e.getMessage());
        }
    }

    private String resolveDisplayName(Map<String, Object> profile, User existing, Long id) {
        Object displayName = profile.get("displayName");
        if (displayName != null) {
            return String.valueOf(displayName);
        }
        if (existing != null && existing.getDisplayName() != null && !existing.getDisplayName().isBlank()) {
            return existing.getDisplayName();
        }
        return "user-" + id;
    }

    private Map<String, Object> buildResponse(Long id, String displayName, String profileJson) {
        Map<String, Object> profileMap;
        try {
            profileMap = objectMapper.readValue(profileJson == null ? "{}" : profileJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            profileMap = Map.of();
        }
        Map<String, Object> response = new HashMap<>(profileMap);
        response.put("id", id);
        response.put("displayName", displayName);
        return response;
    }
}
