package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.config.ProfileImageDirectoryInitializerConfig;
import com.moneta.hub.moneta.model.entity.MonetaUser;
import com.moneta.hub.moneta.model.entity.UserStock;
import com.moneta.hub.moneta.model.entity.Verification;
import com.moneta.hub.moneta.model.enums.UserRole;
import com.moneta.hub.moneta.model.enums.UserStatus;
import com.moneta.hub.moneta.model.enums.VerificationStatus;
import com.moneta.hub.moneta.model.message.request.UserRequest;
import com.moneta.hub.moneta.model.message.response.QuoteResponse;
import com.moneta.hub.moneta.model.message.response.UserResponse;
import com.moneta.hub.moneta.repository.MonetaUserRepository;
import com.moneta.hub.moneta.repository.RoleRepository;
import com.moneta.hub.moneta.repository.UserStockRepository;
import com.moneta.hub.moneta.repository.VerificationRepository;
import com.moneta.hub.moneta.security.JwtGenerator;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final MonetaUserRepository userRepository;

    private final RoleRepository roleRepository;

    private final VerificationRepository verificationRepository;

    private final UserStockRepository userStockRepository;

    private final EmailService emailService;

    private final FinanceService financeService;

    private final ProfileImageDirectoryInitializerConfig profileImageDirectoryConfig;

    private final JwtGenerator jwtGenerator;

    private final PasswordEncoder encoder;

    private static final Long VERIFICATION_EXPIRATION_DAYS = 5L;
    private static final List<String> SUPPORTED_IMAGE_MIME_TYPES = List.of(MediaType.IMAGE_PNG_VALUE,
                                                                           MediaType.IMAGE_JPEG_VALUE);

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

    @Transactional
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

    @Transactional
    public UserResponse getUserDataWithToken(HttpServletRequest httpServletRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   IOException {
        log.debug("Extracting JWT token from HTTP servlet request.");
        String jwtToken = SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest);
        String username = jwtGenerator.getUsernameFromToken(jwtToken);
        log.debug("Extracted encrypted username from token.");

        MonetaUser user = userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user by username."));
        UserResponse response = UserResponse.mapAuthenticatedUserEntity(user, jwtToken);
        response.setUsername(SecurityUtil.decryptUsername(username));
        response.setImageBase64(getProfileImageForUser(user.getId()));
        log.debug("Successfully mapped user entity.");

        return response;
    }

    @Transactional
    public void deleteUserAccountWithId(Long id) throws IOException {
        log.debug("Delete account for user with ID: {}", id);
        deleteProfileImageForUserWithId(id);
        MonetaUser user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user with ID: " + id));
        verificationRepository.deleteAllByUserId(id);
        userRepository.delete(user);
        log.debug("Deleted account with ID: {}", id);
    }

    @Transactional
    public byte[] saveProfileImageForUserWithId(MultipartFile profileImage, Long id)
            throws HttpMediaTypeNotSupportedException, IOException {
        MonetaUser user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user with ID: " + id));
        log.debug("Saving profile image for user with ID: {}", id);
        this.validateProfileImage(profileImage);
        String imageName = this.generateUserProfileImagePath(profileImage, id);
        Path profileImagePath = Path.of(profileImageDirectoryConfig.getProfileImageDirectory(), imageName);
        if (user.getProfilePicture() != null) {
            String oldProfileImagePath = user.getProfilePicture();
            Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("New profile image saved successfully for user with ID: {}.", id);
            user.setProfilePicture(imageName);
            userRepository.save(user);
            this.deleteOldProfilePicture(oldProfileImagePath, id);
            return Files.readAllBytes(profileImagePath);
        }
        Files.copy(profileImage.getInputStream(), profileImagePath, StandardCopyOption.REPLACE_EXISTING);
        log.debug("Profile image saved successfully for user with ID: {}.", id);
        user.setProfilePicture(imageName);
        userRepository.save(user);
        return Files.readAllBytes(profileImagePath);
    }

    private void deleteOldProfilePicture(String oldProfileImagePath, Long id) throws IOException {
        Path oldProfilePicture = Path.of(profileImageDirectoryConfig.getProfileImageDirectory(), oldProfileImagePath);
        if (Files.exists(oldProfilePicture)) {
            Files.delete(oldProfilePicture);
            log.debug("Successfully deleted old profile image for user with ID: {}.", id);
        }
    }

    private void validateProfileImage(MultipartFile profileImage) {
        log.debug("Validating profile image.");
        if (profileImage.isEmpty()) {
            throw new IllegalArgumentException("Profile Image is not updated.");
        }
        if (!SUPPORTED_IMAGE_MIME_TYPES.contains(profileImage.getContentType())) {
            throw new IllegalArgumentException("Invalid image MIME type.");
        }
        if ((profileImage.getSize() / (1024.0 * 1024.0)) > 2.0) {
            throw new IllegalArgumentException("Profile image needs to be less than 2 MB in size.");
        }
        log.debug("Image successfully validated.");
    }

    private String generateUserProfileImagePath(MultipartFile profileImage, Long id) throws HttpMediaTypeNotSupportedException {
        log.debug("Generating profile image path name.");
        String imageOriginalFileName = profileImage.getOriginalFilename();
        if (imageOriginalFileName != null) {
            return "PI" + id + "_" + UUID.randomUUID() + imageOriginalFileName.substring(imageOriginalFileName.lastIndexOf("."));
        }
        throw new HttpMediaTypeNotSupportedException("Invalid image extension.");
    }

    @Transactional
    public void deleteProfileImageForUserWithId(Long id) throws IOException {
        log.debug("Deleting profile image for user with ID: {}", id);
        MonetaUser user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user with ID " + id));
        this.deleteOldProfilePicture(user.getProfilePicture(), id);
        user.setProfilePicture(null);
        userRepository.save(user);
        log.debug("Successfully deleted profile image for user with ID: {}", id);
    }

    public void validateTokenAndUserId(String jwtToken, Long id) {
        log.debug("Validating user JWT token and user ID.");
        String username = jwtGenerator.getUsernameFromToken(jwtToken);
        MonetaUser user = findUserByUsername(username);
        if (user.getId().compareTo(id) != 0) {
            throw new IllegalArgumentException("Invalid JWT token.");
        }
    }

    public byte[] getProfileImageForUser(Long id) throws IOException {
        log.debug("Fetching user profile image.");
        MonetaUser user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user with ID " + id));
        if (user.getProfilePicture() == null) {
            log.debug("User does not have profile image set up.");
            return new byte[0];
        }
        Path profileImagePath = Path.of(profileImageDirectoryConfig.getProfileImageDirectory() + user.getProfilePicture());
        log.debug("Returning profile image as byte array.");
        return Files.readAllBytes(profileImagePath);
    }

    @Transactional
    public void changeUserPassword(UserRequest userRequest) {

        MonetaUser user = userRepository.findById(userRequest.getId()).orElseThrow(
                () -> new IllegalArgumentException("Cannot find user with ID " + userRequest.getId()));
        log.debug("Changing password for user with ID: {}", userRequest.getId());
        this.validateUserPasswordChangeRequest(user, userRequest);
        user.setPassword(encoder.encode(userRequest.getPassword()));
        userRepository.save(user);
        log.debug("User password has been changed and saved successfully.");
    }

    private void validateUserPasswordChangeRequest(MonetaUser user, UserRequest userRequest) {
        log.debug("Validating user password change request.");
        if (!Objects.equals(userRequest.getPassword(), userRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords are not matching.");
        }
        if (!encoder.matches(userRequest.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect old password.");
        }

        if (encoder.matches(userRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as old password.");
        }
        log.debug("User password change request validated.");
    }

    public void addStockToUsersFavourites(String jwtToken, String ticker) {
        log.debug("Adding stock {} to users favourites.", ticker);

        MonetaUser user = findUserByUsername(jwtGenerator.getUsernameFromToken(jwtToken));
        Optional<UserStock> stock = userStockRepository.findByTickerAndUserId(ticker.toUpperCase(), user.getId());
        if (stock.isPresent()) {
            throw new IllegalArgumentException("Stock is already in users favourites.");
        }

        UserStock userStock = UserStock.builder()
                                       .ticker(ticker.toUpperCase())
                                       .user(user)
                                       .build();
        userStockRepository.save(userStock);
        log.debug("User stock saved successfully.");
    }

    public void deleteStockFromUserFavourites(String jwtToken, String ticker) {
        log.debug("Removing stock from user favourites.");

        MonetaUser user = findUserByUsername(jwtGenerator.getUsernameFromToken(jwtToken));
        UserStock stock = userStockRepository.findByTickerAndUserId(ticker.toUpperCase(), user.getId()).orElseThrow(
                () -> new IllegalArgumentException("Cannot find " + ticker + "for user."));
        userStockRepository.delete(stock);
        log.debug("Removed stock from users favourites.");
    }

    public List<QuoteResponse> getAllUserFavouriteStocks(String jwtToken) {
        log.debug("Fetching all user stocks.");
        MonetaUser user = findUserByUsername(jwtGenerator.getUsernameFromToken(jwtToken));

        List<UserStock> userStocks = userStockRepository.findAllByUserId(user.getId());
        if (userStocks.isEmpty()) {
            return Collections.emptyList();
        }

        return financeService.fetchQuotesForUserStocks(userStocks);
    }
}
