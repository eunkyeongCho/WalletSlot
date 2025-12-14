package com.ssafy.b108.walletslot.backend.infrastructure.fcm.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.ssafy.b108.walletslot.backend.global.error.AppException;
import com.ssafy.b108.walletslot.backend.global.error.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleAccessTokenUtil {

    // Field
    @Value("${fcm.server.service-account-json-path}")
    private String SERVICE_ACCOUNT_PATH;

    // Method
    public String getAccessToken(){

        System.out.println(SERVICE_ACCOUNT_PATH);

        GoogleCredentials googleCredentials;
        try {
            googleCredentials = GoogleCredentials
                    .fromStream(new ClassPathResource(SERVICE_ACCOUNT_PATH).getInputStream())
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
            googleCredentials.refreshIfExpired();
        } catch(Exception e) {
            e.printStackTrace();
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "GoogleAccessTokenUtil - 001");
        }
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
