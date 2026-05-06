package com.li.bbs.auth.dto;

public record LoginResponse(String token, long expiresIn) {
}

