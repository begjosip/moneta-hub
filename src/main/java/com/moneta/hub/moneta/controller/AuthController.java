package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import com.moneta.hub.moneta.model.message.request.UserRequest;
import com.moneta.hub.moneta.model.message.request.validator.UserRequestValidator;
import com.moneta.hub.moneta.model.message.response.UserResponse;
import com.moneta.hub.moneta.security.JwtGenerator;
import com.moneta.hub.moneta.service.UserService;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtGenerator jwtGenerator;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Validated(UserRequestValidator.Login.class) @RequestBody UserRequest userRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        log.info(" > > > POST /api/v1/auth/login");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(SecurityUtil.encryptUsername(userRequest.getUsername().toLowerCase()),
                                                        userRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        MonetaUser user = userService.findUserByUsername(SecurityUtil.encryptUsername(userRequest.getUsername().toLowerCase()));
        String accessToken = jwtGenerator.generateToken(authentication, user);
        UserResponse response = UserResponse.mapAuthenticatedUserEntity(user, accessToken);
        response.setImageBase64(userService.getProfileImageForUser(user.getId()));
        log.info(" < < < POST /api/v1/auth/login");

        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Validated(UserRequestValidator.Register.class) @RequestBody UserRequest userRequest)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   MessagingException, UnsupportedEncodingException {

        log.info(" > > > POST /api/v1/auth/login");

        MonetaUser user = userService.registerUser(userRequest);

        log.info(" < < < POST /api/v1/auth/login");

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                                                                 .path("/{userId}")
                                                                 .buildAndExpand(user.getId())
                                                                 .toUri())
                             .build();
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<Object> verifyUser(@PathVariable String token) {

        log.info(" > > > POST /api/v1/auth/verify/{}", token);
        String htmlContent = userService.verifyUserWithToken(token);
        log.info(" < < < POST /api/v1/auth/verify/{}", token);

        return ResponseEntity.ok(htmlContent);
    }

    @PostMapping("/reset/request")
    public ResponseEntity<Object> requestPasswordReset(@Validated(UserRequestValidator.PasswordReset.class) @RequestBody UserRequest request)
            throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException,
                   MessagingException, UnsupportedEncodingException {
        log.info(" > > > POST /api/v1/auth/reset/request");
        userService.requestPasswordResetForUser(request.getUsername().toLowerCase());
        log.info(" < < < POST /api/v1/auth/reset/request");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset/forgotten")
    public ResponseEntity<Object> resetPasswordWithToken(@Validated(UserRequestValidator.NewPassword.class) @RequestBody UserRequest request) {
        log.info(" > > > POST /api/v1/auth/reset/forgotten");

        userService.changeForgottenPassword(request);

        log.info(" < < < POST /api/v1/auth/verify/forgotten");
        return ResponseEntity.ok().build();
    }
}
