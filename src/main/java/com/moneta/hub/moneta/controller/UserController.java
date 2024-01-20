package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.message.request.UserRequest;
import com.moneta.hub.moneta.model.message.request.validator.UserRequestValidator;
import com.moneta.hub.moneta.model.message.response.UserResponse;
import com.moneta.hub.moneta.service.UserService;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/validate")
    public ResponseEntity<Object> validateUser() {

        log.info(" > > > GET /api/v1/user/validate");
        log.info("User is validated with JWT token.");
        log.info(" < < < GET /api/v1/user/validate");

        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<UserResponse> getUser(@NonNull HttpServletRequest httpServletRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   IOException {

        log.info(" > > > GET /api/v1/user");
        UserResponse response = userService.getUserDataWithToken(httpServletRequest);
        log.info(" < < < GET /api/v1/user");

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> deleteUserAccountWithId(@NonNull HttpServletRequest httpServletRequest,
                                                          @PathVariable Long id) throws IOException {

        log.info(" > > > DELETE /api/v1/user/{}", id);
        userService.validateTokenAndUserId(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), id);
        userService.deleteUserAccountWithId(id);
        log.info(" < < < DELETE /api/v1/user/{}", id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> changeUserPassword(@NonNull HttpServletRequest httpServletRequest,
                                                     @Validated(UserRequestValidator.PasswordChange.class)
                                                     @RequestBody UserRequest userRequest) {

        log.info(" > > > PUT /api/v1/user/password");
        userService.validateTokenAndUserId(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), userRequest.getId());
        userService.changeUserPassword(userRequest);
        log.info(" < < < PUT /api/v1/user/password");

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/image")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> uploadUserProfileImage(@NotNull @RequestParam("userId") Long id,
                                                         @NotNull @RequestPart("profileImage") MultipartFile profileImage,
                                                         @NotNull HttpServletRequest httpServletRequest)
            throws HttpMediaTypeNotSupportedException, IOException {
        log.info(" > > > POST /api/v1/user/image");
        userService.validateTokenAndUserId(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), id);
        byte[] image = userService.saveProfileImageForUserWithId(profileImage, id);
        String base64Image = Base64.getEncoder().encodeToString(image);
        log.info(" < < < POST /api/v1/user/image");

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                                                                 .path("/{id}")
                                                                 .buildAndExpand(id)
                                                                 .toUri())
                             .body(base64Image);
    }

    @GetMapping(value = "/image/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> getUserProfileImage(@PathVariable Long id,
                                                      @NotNull HttpServletRequest httpServletRequest) throws IOException {
        log.info(" > > > GET /api/v1/user/image/{}", id);
        userService.validateTokenAndUserId(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), id);
        byte[] image = userService.getProfileImageForUser(id);
        String base64Image = Base64.getEncoder().encodeToString(image);
        log.info(" < < < GET /api/v1/user/image/{}", id);

        return ResponseEntity.ok().body(base64Image);
    }

    @DeleteMapping(value = "/image/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> deleteProfileImageForUserWithId(@NotNull HttpServletRequest httpServletRequest,
                                                                  @PathVariable Long id) throws IOException {
        log.info(" > > > DELETE /api/v1/user/image/{}", id);
        userService.validateTokenAndUserId(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), id);
        userService.deleteProfileImageForUserWithId(id);
        log.info(" < < < DELETE /api/v1/user/image/{}", id);

        return ResponseEntity.noContent().build();
    }
}
