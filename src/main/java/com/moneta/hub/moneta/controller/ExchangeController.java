package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.entity.Currency;
import com.moneta.hub.moneta.model.message.request.ExchangeRequest;
import com.moneta.hub.moneta.model.message.response.CurrencyResponse;
import com.moneta.hub.moneta.model.message.response.ExchangeResponse;
import com.moneta.hub.moneta.service.ExchangeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/exchange")
@RequiredArgsConstructor
@Slf4j
public class ExchangeController {

    private final ExchangeService exchangeService;

    @GetMapping
    public ResponseEntity<Object> getAllCurrencies() {

        log.info(" > > > GET /api/v1/exchange");
        List<Currency> currencyList = exchangeService.getAllCurrencies();
        log.info(" < < < GET /api/v1/exchange");

        return ResponseEntity.ok(currencyList.stream().map(CurrencyResponse::mapEntityToCurrencyResponse).toList());
    }

    @PostMapping
    public ResponseEntity<Object> exchangeCurrencies(@Validated @RequestBody ExchangeRequest request) {

        log.info(" > > > POST /api/v1/exchange");
        ExchangeResponse response = exchangeService.exchangeCurrencies(request);
        log.info(" < < < POST /api/v1/exchange");

        return ResponseEntity.ok(response);
    }
}
