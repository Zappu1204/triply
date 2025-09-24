package com.triply.tripapp.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.triply.tripapp.common.exception.BadRequestException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleOAuth2Service {

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    public GoogleUserInfo verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setId(payload.getSubject());
                userInfo.setEmail(payload.getEmail());
                userInfo.setName((String) payload.get("name"));
                userInfo.setPicture((String) payload.get("picture"));
                userInfo.setEmailVerified(payload.getEmailVerified());
                return userInfo;
            }
            throw new BadRequestException("Invalid Google ID token");
        } catch (Exception e) {
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage());
        }
    }

    @Data
    public static class GoogleUserInfo {
        private String id;
        private String email;
        private String name;
        private String picture;
        private Boolean emailVerified;
    }
}






