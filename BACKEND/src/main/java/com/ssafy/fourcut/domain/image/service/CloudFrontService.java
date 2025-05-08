package com.ssafy.fourcut.domain.image.service;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class CloudFrontService {

    @Value("${cloudfront.domain}")
    private String cloudFrontDomain;

    @Value("${cloudfront.keyPairId}")
    private String keyPairId;

    @Value("${cloudfront.privateKeyPath}")
    private String privateKeyPath;

    public String generateSignedCloudFrontUrl(String s3Key) {
        try {
            System.out.println("s3Key::: " + s3Key);
            System.out.println(cloudFrontDomain);
            System.out.println(keyPairId);
            System.out.println(privateKeyPath);

//            Date expiration = new Date(System.currentTimeMillis() + (1000 * 60 * 20)); // 20분
            Date expiration = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 7 * 7)); // 7주일
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