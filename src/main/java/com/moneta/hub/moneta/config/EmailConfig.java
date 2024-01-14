package com.moneta.hub.moneta.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class EmailConfig {

    private final EmailProperties emailProperties;

    @Bean
    public JavaMailSender mailSender() {

        final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailProperties.getHost());
        mailSender.setPort(emailProperties.getPort());
        mailSender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        mailSender.setUsername(emailProperties.getMonetaMail());
        mailSender.setPassword(emailProperties.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.starttls.enable", emailProperties.getTlsEnable());
        props.put("mail.smtp.auth", emailProperties.getAuth());
        props.put("mail.debug", "true");
        props.put("mail.trace", "true");

        return mailSender;
    }
}
