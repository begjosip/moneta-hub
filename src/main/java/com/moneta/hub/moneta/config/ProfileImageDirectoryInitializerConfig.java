package com.moneta.hub.moneta.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Getter
@Slf4j
public class ProfileImageDirectoryInitializerConfig implements ApplicationRunner {

    @Value("${profile.images.directory.path}")
    private String profileImageDirectory;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path directoryPath = Paths.get(profileImageDirectory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
            log.info("Profile image directory created.");
        } else {
            log.info("Profile image directory already exists {}", profileImageDirectory);
        }
    }
}
