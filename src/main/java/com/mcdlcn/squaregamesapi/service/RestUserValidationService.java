package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.exception.ForbiddenActionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Deprecated
@Service
public class RestUserValidationService implements UserValidationService {

    private final RestClient restClient;

    public RestUserValidationService(@Value("${users.api.url}") String usersApiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(usersApiUrl)
                .build();
    }

    @Override
        public void validateUser(UUID userId) {
        Boolean isValid = restClient.get()
                .uri("/users/{id}/valid", userId)
                .retrieve()
                .body(Boolean.class);

        if (!Boolean.TRUE.equals(isValid)) {
            throw new ForbiddenActionException("Invalid user");
        }
    }
}