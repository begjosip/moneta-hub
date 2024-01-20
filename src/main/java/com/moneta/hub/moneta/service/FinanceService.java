package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.config.FinanceProperties;
import com.moneta.hub.moneta.model.entity.BlueChip;
import com.moneta.hub.moneta.model.message.response.AggregatesResponse;
import com.moneta.hub.moneta.model.message.response.CompanyProfileResponse;
import com.moneta.hub.moneta.model.message.response.MarketStatus;
import com.moneta.hub.moneta.model.message.response.NewsResponse;
import com.moneta.hub.moneta.model.message.response.QuoteResponse;
import com.moneta.hub.moneta.model.message.response.SearchResponse;
import com.moneta.hub.moneta.repository.BlueChipRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinanceService {

    private final BlueChipRepository blueChipRepository;

    private final FinanceProperties financeProperties;

    private static final Long TIMEOUT = 5L;

    private static final String NEWS_FETCH_URI = "/news?category=general";
    private static final String COMPANY_PROFILE_URI = "/stock/profile2?symbol=";
    private static final String QUOTE_URI = "/quote?symbol=";
    private static final String MARKET_STATUS_URI = "/stock/market-status?exchange=US";
    private static final String SYMBOL_LOOKUP_URI = "/search?q=";
    private static final String POLYGON_AGGREGATES_URI = "/v2/aggs/ticker/{stocksTicker}/range/{multiplier}/{timespan}/{from}/{to}";
    private static final String POLYGON_AGGREGATES_PROPERTIES = "?adjusted=true&sort=asc&limit=50000";
    private static final String FINNHUB_TOKEN_HEADER_KEY = "X-Finnhub-Token";
    private static final String POLYGON_AUTHORIZATION_HEADER_KEY = "Authorization";
    private static final String COMMON_STOCK_TYPE = "Common Stock";

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

    public List<QuoteResponse> fetchBlueChipStocks() {

        String uri = financeProperties.getFinnhubUrl().concat(QUOTE_URI);
        List<BlueChip> blueChips = blueChipRepository.findAll();
        log.debug("Fetching company profile quote. {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();
        List<QuoteResponse> quoteResponses = new ArrayList<>();
        blueChips.forEach(blueChip -> {
            QuoteResponse quoteResponse = webClient.get()
                                                   .uri(uri.concat(blueChip.getTicker()))
                                                   .header(FINNHUB_TOKEN_HEADER_KEY, financeProperties.getFinnhubApiKey())
                                                   .retrieve()
                                                   .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                                             response -> Mono.error(new IllegalArgumentException(
                                                                     "Too many API requests. Try again later of improve " +
                                                                     "your subscription" +
                                                                     " plan.")))
                                                   .onStatus(status -> !status.is2xxSuccessful(),
                                                             response -> Mono.error(new IllegalArgumentException(
                                                                     "Error occurred while trying to fetch company quote data.")))
                                                   .bodyToMono(QuoteResponse.class)
                                                   .block();
            Objects.requireNonNull(quoteResponse).setCompanyName(blueChip.getCompanyName());
            quoteResponse.setTicker(blueChip.getTicker());
            quoteResponses.add(quoteResponse);
        });
        log.debug("Fetching company profile quotes data success GET > > > {}", uri);
        return quoteResponses;
    }

    public MarketStatus fetchMarketStatus() {
        String uri = financeProperties.getFinnhubUrl().concat(MARKET_STATUS_URI);
        log.debug("Fetching market status data GET > > > {}", uri);

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
                        .bodyToMono(MarketStatus.class)
                        .block();
    }

    public SearchResponse lookupForStockByTicker(String ticker) {
        String uri = financeProperties.getFinnhubUrl().concat(SYMBOL_LOOKUP_URI).concat(ticker);
        log.debug("Fetching symbol lookup data GET > > > {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();

        SearchResponse searchResponse = webClient.get()
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
                                                                   "Error occurred while trying to fetch symbol lookup data.")))
                                                 .bodyToMono(SearchResponse.class)
                                                 .block();
        if (searchResponse != null && searchResponse.getResult() != null) {
            searchResponse.setResult(searchResponse.getResult()
                                                   .stream()
                                                   .filter(stockSearchResult -> stockSearchResult.getType().equals(COMMON_STOCK_TYPE))
                                                   .toList());
        }
        return searchResponse;
    }

    /**
     * @param ticker
     *         Describes Stock ticker symbol
     * @param multiplier
     *         Multiplier value for multiplying timespan
     * @param timespan
     *         Timespan of data range
     * @param from
     *         Start date
     * @param to
     *         End date
     * @return Aggregates response (candle stick data)
     */
    public AggregatesResponse getAggregatesForTickerWithMultiplierInRange(String ticker,
                                                                          Long multiplier,
                                                                          String timespan,
                                                                          LocalDate from,
                                                                          LocalDate to) {

        String uri = financeProperties.getPolygonUrl()
                                      .concat(POLYGON_AGGREGATES_URI)
                                      .replace("{stocksTicker}", ticker.toUpperCase())
                                      .replace("{multiplier}", multiplier.toString())
                                      .replace("{timespan}", timespan)
                                      .replace("{from}", from.toString())
                                      .replace("{to}", to.toString())
                                      .concat(POLYGON_AGGREGATES_PROPERTIES);

        log.debug("Fetching stock aggregates data from uri > > > GET {}", uri);

        WebClient webClient = WebClient.builder()
                                       .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
                                                                                                 .responseTimeout(
                                                                                                         Duration.ofSeconds(TIMEOUT))))
                                       .build();

        AggregatesResponse aggregatesResponse = webClient.get()
                                                         .uri(uri)
                                                         .header(POLYGON_AUTHORIZATION_HEADER_KEY,
                                                                 "Bearer " + financeProperties.getPolygonApiKey())
                                                         .retrieve()
                                                         .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                                                   response -> Mono.error(new IllegalArgumentException(
                                                                           "Too many API requests. Try again later of improve " +
                                                                           "your subscription" +
                                                                           " plan.")))
                                                         .onStatus(status -> !status.is2xxSuccessful(),
                                                                   response -> Mono.error(new IllegalArgumentException(
                                                                           "Error occurred while trying to fetch symbol aggregates data.")))
                                                         .bodyToMono(AggregatesResponse.class)
                                                         .block();
        if (aggregatesResponse != null && (aggregatesResponse.getQueryCount() == 0)) {
            throw new IllegalArgumentException("Invalid ticker name. Cannot get aggregates data.");
        }
        if (aggregatesResponse != null && aggregatesResponse.getNextUrl() != null) {
            String nextRequestUrl = aggregatesResponse.getNextUrl();
            while (!nextRequestUrl.isBlank()) {
                log.debug("Fetching next aggregate data from {}", nextRequestUrl);
                AggregatesResponse nextResponse = webClient.get()
                                                           .uri(nextRequestUrl)
                                                           .header(POLYGON_AUTHORIZATION_HEADER_KEY,
                                                                   "Bearer " + financeProperties.getPolygonApiKey())
                                                           .retrieve()
                                                           .onStatus(status -> status.value() == HttpStatus.TOO_MANY_REQUESTS.value(),
                                                                     response -> Mono.error(new IllegalArgumentException(
                                                                             "Too many API requests. Try again later of improve " +
                                                                             "your subscription" +
                                                                             " plan.")))
                                                           .onStatus(status -> !status.is2xxSuccessful(),
                                                                     response -> Mono.error(new IllegalArgumentException(
                                                                             "Error occurred while trying to fetch symbol aggregates data" +
                                                                             ".")))
                                                           .bodyToMono(AggregatesResponse.class)
                                                           .block();
                if (nextResponse != null && nextResponse.getResults() != null) {
                    aggregatesResponse.addQuoteResponse(nextResponse.getResults());
                    if (nextResponse.getNextUrl() == null) {
                        nextRequestUrl = "";
                    } else {
                        nextRequestUrl = nextResponse.getNextUrl();
                    }
                } else {
                    nextRequestUrl = "";
                }
            }
        }

        return aggregatesResponse;
    }
}
