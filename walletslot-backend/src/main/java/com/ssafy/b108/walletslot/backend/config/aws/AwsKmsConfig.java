// src/main/java/.../config/aws/AwsKmsConfig.java
package com.ssafy.b108.walletslot.backend.config.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;

@Configuration
public class AwsKmsConfig {
    @Bean
    public KmsClient kmsClient(@Value("${cloud.aws.region:ap-northeast-2}") String region) {
        return KmsClient.builder().region(Region.of(region)).build();
    }
}
