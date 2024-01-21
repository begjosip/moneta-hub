package com.moneta.hub.moneta.model.message.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaqRequest {

    @NotBlank
    private String question;

    @NotBlank
    private String answer;
}
