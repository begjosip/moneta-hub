package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

@Data
public class ExchangeResponse {

    private String result;

    private Long timeLastUpdateUnix;

    private String baseCode;

    private String targetCode;

    private Double conversionRate;

    private Double conversionResult;
}
