package com.inmobiliaria.cloud.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials:}")
    private String credentialsPath;

    @PostConstruct
    public void initializeFirebase() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }
        String path = credentialsPath.isBlank() ? System.getenv("GOOGLE_APPLICATION_CREDENTIALS") : credentialsPath;
        if (path == null || path.isBlank()) {
            return;
        }
        if (!Files.isRegularFile(Path.of(path))) {
            throw new IllegalStateException("No existe el archivo de credenciales de Firebase: " + path);
        }
        try (FileInputStream credentials = new FileInputStream(path)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }
}
