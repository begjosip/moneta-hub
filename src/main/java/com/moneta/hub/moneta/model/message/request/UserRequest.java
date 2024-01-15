package com.moneta.hub.moneta.model.message.request;

import com.moneta.hub.moneta.model.message.request.validator.UserRequestValidator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {

    @Null(groups = {UserRequestValidator.Login.class, UserRequestValidator.Register.class})
    private Long id;

    @Null(groups = {UserRequestValidator.Login.class})
    @NotBlank(groups = {UserRequestValidator.Register.class})
    private String firstName;

    @Null(groups = {UserRequestValidator.Login.class})
    @NotBlank(groups = {UserRequestValidator.Register.class})
    private String lastName;

    @NotBlank(groups = {UserRequestValidator.Login.class, UserRequestValidator.Register.class, UserRequestValidator.PasswordReset.class})
    @Email(groups = {UserRequestValidator.Login.class, UserRequestValidator.Register.class, UserRequestValidator.PasswordReset.class})
    private String username;

    /**
     * Password needs to be at least 8 characters long and contain letters and digits
     */
    @NotBlank(groups = {UserRequestValidator.Login.class, UserRequestValidator.Register.class, UserRequestValidator.NewPassword.class})
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$",
             groups = {UserRequestValidator.Register.class, UserRequestValidator.NewPassword.class})
    private String password;

    @Null(groups = {UserRequestValidator.Login.class})
    @NotBlank(groups = {UserRequestValidator.Register.class, UserRequestValidator.NewPassword.class})
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$",
             groups = {UserRequestValidator.Register.class, UserRequestValidator.NewPassword.class})
    private String confirmPassword;

    @NotBlank(groups = {UserRequestValidator.NewPassword.class})
    private String token;
}
