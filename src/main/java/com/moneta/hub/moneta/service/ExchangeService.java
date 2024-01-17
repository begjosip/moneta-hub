package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.config.ExchangeProperties;
import com.moneta.hub.moneta.model.entity.Currency;
import com.moneta.hub.moneta.model.message.request.ExchangeRequest;
import com.moneta.hub.moneta.model.message.response.ExchangeResponse;
import com.moneta.hub.moneta.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private final ExchangeProperties exchangeProperties;

    private final CurrencyRepository currencyRepository;

    private static final Long TIMEOUT = 5L;

    private static final String PAIR_URI_ARG = "/pair/";

    public List<Currency> getAllCurrencies() {
        log.debug("Fetching all currencies from database.");
        return currencyRepository.findAll();
    }

    public ExchangeResponse exchangeCurrencies(ExchangeRequest request) {
        String uri = exchangeProperties.getExchangeUrl()
                                       .concat(exchangeProperties.getExchangeApiKey())
                                       .concat(PAIR_URI_ARG)
                                       .concat("/" + request.getBaseCode())
                                       .concat("/" + request.getTargetCode());

        log.debug("Fetching exchange rates data GET > > > {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();

        return webClient.get()
                        .uri(uri)
                        .retrieve()
                        .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Too many API requests. Try again later of improve " +
                                          "your subscription" +
                                          " plan.")))
                        .onStatus(status -> !status.is2xxSuccessful(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Error occurred while trying to fetch company data.")))
                        .bodyToMono(ExchangeResponse.class)
                        .block();
    }
}
