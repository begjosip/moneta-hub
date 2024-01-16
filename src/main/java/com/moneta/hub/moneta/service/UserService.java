package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import com.moneta.hub.moneta.model.entity.Verification;
import com.moneta.hub.moneta.model.enums.UserRole;
import com.moneta.hub.moneta.model.enums.UserStatus;
import com.moneta.hub.moneta.model.enums.VerificationStatus;
import com.moneta.hub.moneta.model.message.request.UserRequest;
import com.moneta.hub.moneta.repository.MonetaUserRepository;
import com.moneta.hub.moneta.repository.RoleRepository;
import com.moneta.hub.moneta.repository.VerificationRepository;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final MonetaUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final VerificationRepository verificationRepository;

    private final EmailService emailService;

    private final PasswordEncoder encoder;

    private static final Long VERIFICATION_EXPIRATION_DAYS = 5L;

    public MonetaUser findUserByUsername(String username) {
        log.debug("Find user by username {}", username);
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User does not exist."));
    }

    @Transactional
    public MonetaUser registerUser(UserRequest userRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   MessagingException, UnsupportedEncodingException {

        log.debug("Registration user from request.");
        this.validateUserRegistrationRequest(userRequest);
        MonetaUser user = MonetaUser.builder()
                                    .firstName(userRequest.getFirstName())
                                    .lastName(userRequest.getLastName())
                                    .username(SecurityUtil.encryptUsername(userRequest.getUsername()))
                                    .password(encoder.encode(userRequest.getPassword()))
                                    .status(UserStatus.PENDING_CONFIRMATION)
                                    .roles(roleRepository.findAllByNameIn(List.of(UserRole.USER)))
                                    .build();

        log.debug("Saving user with username {}", user.getUsername());
        MonetaUser monetaUser = userRepository.save(user);

        Verification verification = Verification.builder()
                                                .status(VerificationStatus.PENDING)
                                                .token(UUID.randomUUID().toString())
                                                .user(monetaUser)
                                                .build();

        log.debug("Saving verification for user {}", monetaUser.getUsername());
        verificationRepository.save(verification);
        emailService.sendVerificationEmail(verification);
        log.debug("Successfully created new user with ID:{}", monetaUser.getId());
        return monetaUser;
    }

    private void validateUserRegistrationRequest(UserRequest userRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        log.debug("Validating user request.");
        if (userRepository.findByUsername(SecurityUtil.encryptUsername(userRequest.getUsername())).isPresent()) {
            throw new IllegalArgumentException("User with given username already exists.");
        }
        if (!Objects.equals(userRequest.getPassword(), userRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords are not matching.");
        }
        log.debug("User registration request validated.");
    }

    @Transactional
    public String verifyUserWithToken(String token) {
        log.debug("Verifying user with token {}", token);
        try {
            if (token.length() != 36) {
                throw new IllegalArgumentException("Invalid validation token length.");
            }
            Verification verification = verificationRepository.findByTokenAndStatus(token, VerificationStatus.PENDING).orElseThrow(
                    () -> new IllegalArgumentException("Invalid or expired token."));
            if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusDays(VERIFICATION_EXPIRATION_DAYS))) {
                log.debug("Verification expired.");
                verification.setStatus(VerificationStatus.EXPIRED);
                MonetaUser user = verification.getUser();
                invalidateUser(user);
                verificationRepository.save(verification);
                throw new IllegalArgumentException("Verification token expired. Register again.");
            }
            MonetaUser user = verification.getUser();
            user.setStatus(UserStatus.ACTIVE);
            verification.setStatus(VerificationStatus.VERIFIED);
            userRepository.save(user);
            verificationRepository.save(verification);

            log.debug("Successfully validated user with ID: ");
            return EmailService.VERIFICATION_SUCCESS_HTML;
        } catch (Exception ex) {
            return EmailService.VERIFICATION_UNSUCCESSFUL_HTML;
        }
    }

    private void invalidateUser(MonetaUser user) {
        log.debug("Invalidating user with ID: {}", user.getId());
        user.setStatus(UserStatus.DELETED);
        user.setUsername(user.getUsername() + "_" + user.getId());
        userRepository.save(user);
        log.debug("User with ID: {} invalidated.", user.getId());
    }

    @Transactional
    public void requestPasswordResetForUser(String username)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   MessagingException, UnsupportedEncodingException {
        log.debug("Requested password change for user.");

        MonetaUser user = findUserByUsername(SecurityUtil.encryptUsername(username));
        if (verificationRepository.findAllByUserIdAndStatus(user.getId(), VerificationStatus.PENDING)
                                         .size() > 5L) {
            throw new IllegalArgumentException("You already have too many unused password requests. Contact our team.");
        }
        Verification verification = verificationRepository.save(Verification.builder()
                                                                            .token(UUID.randomUUID().toString())
                                                                            .status(VerificationStatus.PENDING)
                                                                            .user(user)
                                                                            .build());
        log.debug("Saved verification with ID: {}", verification.getId());
        emailService.sendPasswordChangeRequestEmail(verification);
    }

    public void changeForgottenPassword(UserRequest request) {
        log.debug("Change user forgotten password.");
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password are not matching.");
        }
        Verification verification = verificationRepository.findByTokenAndStatus(request.getToken(), VerificationStatus.PENDING)
                                                          .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (verification.getCreatedAt().isBefore(LocalDateTime.now().minusDays(2L))) {
            verification.setStatus(VerificationStatus.EXPIRED);
            verificationRepository.save(verification);
            throw new IllegalArgumentException("Reset request has been sent before two days. Request password reset again.");
        }
        MonetaUser user = verification.getUser();
        user.setPassword(encoder.encode(request.getPassword()));
        userRepository.save(user);
        log.debug("Saved new password for user with ID:{}", user.getId());
    }
}
