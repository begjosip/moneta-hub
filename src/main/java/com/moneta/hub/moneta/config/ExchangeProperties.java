package com.moneta.hub.moneta.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class ExchangeProperties {

    @Value("${exchange.rate.url}")
    private String exchangeUrl;

    @Value("${exchange.rate.api.key}")
    private String exchangeApiKey;
}
