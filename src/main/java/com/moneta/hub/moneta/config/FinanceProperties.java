package com.moneta.hub.moneta.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class FinanceProperties {

    @Value("${polygon.api.url}")
    private String polygonUrl;

    @Value("${polygon.api.key}")
    private String polygonApiKey;

    @Value("${finnhub.io.url}")
    private String finnhubUrl;

    @Value("${finnhub.io.api.key}")
    private String finnhubApiKey;

}
