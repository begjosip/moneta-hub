package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.config.EmailProperties;
import com.moneta.hub.moneta.model.entity.Verification;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private static final String MONETA_STOCKS = "Moneta Invest";

    private final JavaMailSender javaMailSender;

    private final EmailProperties emailProperties;

    private final SpringTemplateEngine springTemplateEngine;

    private static final String VERIFICATION_TEMPLATE = "verification";

    private static final String FIRSTNAME = "firstName";
    private static final String VERIFICATION_VARIABLE = "verificationUrl";

    public static final String VERIFICATION_SUCCESS_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verification Success</title>
            </head>
            <body style="text-align: center; background-color: #343434; font-family: 'Arial', serif; color: #FFFFFF; padding: 50px;">
                <h1>You successfully verified your account!</h1>
                <script>
                  setTimeout(function() {
                      window.close();
                  }, 2000);
                </script>
            </body>
            </html>
            """;

    public static final String VERIFICATION_UNSUCCESSFUL_HTML = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Verification Unsuccessful</title>
            </head>
            <body style="text-align: center; background-color: #343434; font-family: 'Arial', serif; color: #FFFFFF; padding: 50px;">
                <h1>Try again later or register again!</h1>
                <br/>
                <h2>Your token is expired or invalid</h2>
                <script>
                  setTimeout(function() {
                      window.close();
                  }, 5000);
                </script>
            </body>
            </html>
            """;

    public void sendVerificationEmail(Verification verification)
            throws MessagingException, UnsupportedEncodingException, NoSuchPaddingException, IllegalBlockSizeException,
                   NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        log.debug("Sending verification email to user with ID:{}", verification.getUser().getId());
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,
                                                         MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                                                         StandardCharsets.UTF_8.name());

        log.debug("Setting context for verification email");
        String userEmail = SecurityUtil.decryptUsername(verification.getUser().getUsername());
        String verificationUrl = emailProperties.getVerificationUrl() + verification.getToken();
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable(FIRSTNAME, verification.getUser().getFirstName());
        context.setVariable(VERIFICATION_VARIABLE, verificationUrl);

        String html = springTemplateEngine.process(VERIFICATION_TEMPLATE, context);
        helper.setSubject("Verify Moneta account");
        helper.setTo(userEmail);
        helper.setText(html, true);
        helper.setSentDate(new Date());
        helper.setFrom(new InternetAddress(emailProperties.getMonetaMail(), MONETA_STOCKS));
        javaMailSender.send(message);

        log.debug("Sent verification email to user with ID:{}", verification.getUser().getId());
    }
}
