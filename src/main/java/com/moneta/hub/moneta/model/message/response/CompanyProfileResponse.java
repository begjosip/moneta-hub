package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CompanyProfileResponse {

    private String country;

    private String currency;

    private String exchange;

    private LocalDate ipo;

    private Long marketCapitalization;

    private String name;

    private String ticker;

    private String weburl;

    private String logo;

    private String finnhubIndustry;
}
