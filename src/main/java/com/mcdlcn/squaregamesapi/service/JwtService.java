package com.mcdlcn.squaregamesapi.service;

import java.util.UUID;

public interface JwtService {

    String extractUsername(String token);

    UUID extractUserId(String token);

    String extractRole(String token);

    boolean isTokenValid(String token);
}