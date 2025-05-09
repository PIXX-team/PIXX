package com.ssafy.fourcut.domain.image.service;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import com.ssafy.fourcut.domain.image.controller.StoreController;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class CloudFrontService {

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${cloudfront.keyPairId}")
    private String keyPairId;

    @Value("${cloudfront.privateKeyPath}")
    private String privateKeyPath;

    private static final Logger log = LoggerFactory.getLogger(CloudFrontService.class);

    public String generateSignedCloudFrontUrl(String s3Key, String usage) {
        try {
            log.info("S3_Key : " + s3Key);
            log.info("cloudFrontDomain : " + cloudFrontDomain);
            log.info("keyPairId : " + keyPairId);
            log.info("privateKeyPath : " + privateKeyPath);

            long time = 1000L * 60 * 5; // 5분
            if(usage.equals("get")) {
                time = (1000 * 60 * 30); // 30분
            }

            Date expiration = new Date(System.currentTimeMillis() + time);
//            File privateKey = new ClassPathResource(privateKeyPath).getFile(); // 로컬 실험용
            File privateKey = new File(privateKeyPath); // 서버용

            return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
                    SignerUtils.Protocol.https,
                    cloudFrontDomain,
                    privateKey,
                    s3Key,
                    keyPairId,
                    expiration
            );
        } catch (Exception e) {
            throw new RuntimeException("CloudFront 서명 URL 생성 실패", e);
        }
    }
}