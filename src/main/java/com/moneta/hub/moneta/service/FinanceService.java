package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.config.FinanceProperties;
import com.moneta.hub.moneta.model.message.response.CompanyProfileResponse;
import com.moneta.hub.moneta.model.message.response.NewsResponse;
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
public class FinanceService {

    private final FinanceProperties financeProperties;

    private static final Long TIMEOUT = 5L;

    private static final String NEWS_FETCH_URI = "/news?category=general";
    private static final String COMPANY_PROFILE_URI = "/stock/profile2?symbol=";
    private static final String FINNHUB_TOKEN_HEADER_KEY = "X-Finnhub-Token";

    public List<NewsResponse> fetchStockMarketNews() {

        String uri = financeProperties.getFinnhubUrl().concat(NEWS_FETCH_URI);
        log.debug("Fetching stock news GET > > > {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();

        return webClient.get()
                        .uri(uri)
                        .header(FINNHUB_TOKEN_HEADER_KEY, financeProperties.getFinnhubApiKey())
                        .retrieve()
                        .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Too many API requests. Try again later of improve your subscription plan.")))
                        .onStatus(status -> !status.is2xxSuccessful(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Error occurred while trying to fetch stock news data.")))
                        .bodyToFlux(NewsResponse.class)
                        .collectList()
                        .block();
    }

    public CompanyProfileResponse fetchCompanyProfile(String ticker) {

        String uri = financeProperties.getFinnhubUrl().concat(COMPANY_PROFILE_URI).concat(ticker);
        log.debug("Fetching company profile data GET > > > {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();

        return webClient.get()
                        .uri(uri)
                        .header(FINNHUB_TOKEN_HEADER_KEY, financeProperties.getFinnhubApiKey())
                        .retrieve()
                        .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Too many API requests. Try again later of improve " +
                                          "your subscription" +
                                          " plan.")))
                        .onStatus(status -> !status.is2xxSuccessful(),
                                  response -> Mono.error(new IllegalArgumentException(
                                          "Error occurred while trying to fetch company data.")))
                        .bodyToMono(CompanyProfileResponse.class)
                        .block();
    }
}
