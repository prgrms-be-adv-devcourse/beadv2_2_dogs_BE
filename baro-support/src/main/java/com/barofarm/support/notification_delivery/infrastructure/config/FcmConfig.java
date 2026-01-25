package com.barofarm.support.notification_delivery.infrastructure.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * firebase Admin SDK 초기화
 *
 * - NOTE : serviceAccount.json은 절대 Git에 안 올리게 주의
 * - /mnt/s3 또는 Secret Manager 기반 경로로 주입 (안전성 고려)
 * */

@Configuration
public class FcmConfig {

    @Value("${notification.delivery.fcm.enabled:false}")
    private boolean enabled;

    @Value("${notification.delivery.fcm.credential-path:/mnt/s3/firebase/serviceAccount.json}")
    private String credentialPath;

    @PostConstruct
    public void init() throws Exception {
        if (!enabled) {
            return;
        }

        // 이미 초기화된 경우 재초기화 방지
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(credentialPath)) {
            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);
        }
    }
}
