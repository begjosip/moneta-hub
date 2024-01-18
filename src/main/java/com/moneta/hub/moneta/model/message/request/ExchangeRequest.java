package com.moneta.hub.moneta.model.message.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRequest {

    @NotBlank
    private String baseCode;

    @NotBlank
    private String targetCode;

    @NotNull
    private BigDecimal amount;

}
