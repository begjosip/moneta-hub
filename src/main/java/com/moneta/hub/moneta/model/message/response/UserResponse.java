package com.moneta.hub.moneta.model.message.response;

import com.moneta.hub.moneta.model.entity.MonetaUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String username;

    private String status;

    private String accessToken;

    private LocalDateTime createdAt;

    private byte[] imageBase64;

    public static UserResponse mapAuthenticatedUserEntity(MonetaUser user, String accessToken) {
        return UserResponse.builder()
                           .id(user.getId())
                           .firstName(user.getFirstName())
                           .lastName(user.getLastName())
                           .accessToken(accessToken)
                           .status(user.getStatus().getName())
                           .createdAt(user.getCreatedAt())
                           .build();
    }
}
