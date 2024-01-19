package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.message.response.CompanyProfileResponse;
import com.moneta.hub.moneta.model.message.response.MarketStatus;
import com.moneta.hub.moneta.model.message.response.NewsResponse;
import com.moneta.hub.moneta.model.message.response.QuoteResponse;
import com.moneta.hub.moneta.model.message.response.SearchResponse;
import com.moneta.hub.moneta.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/finance")
@RequiredArgsConstructor
@Slf4j
public class FinancialController {

    private final FinanceService financeService;

    @GetMapping("/market/status")
    public ResponseEntity<Object> getUsMarketStatus() {

        log.info(" > > > GET /api/v1/finance/market/status");
        MarketStatus response = financeService.fetchMarketStatus();
        log.info(" < < < GET /api/v1/finance/market/status");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/market/news")
    public ResponseEntity<Object> getStockMarketNews() {

        log.info(" > > > GET /api/v1/finance/market/news");
        List<NewsResponse> response = financeService.fetchStockMarketNews();
        log.info(" < < < GET /api/v1/finance/market/news");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/profile/{ticker}")
    public ResponseEntity<Object> getCompanyProfile(@PathVariable String ticker) {

        log.info(" > > > GET /api/v1/finance/company/profile/{}", ticker);
        CompanyProfileResponse response = financeService.fetchCompanyProfile(ticker);
        log.info(" < < < GET /api/v1/finance/company/profile/{}", ticker);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/blue/chips")
    public ResponseEntity<Object> getBlueChipStocks() {

        log.info(" > > > GET /api/v1/finance/blue/chips");
        List<QuoteResponse> response = financeService.fetchBlueChipStocks();
        log.info(" < < < GET /api/v1/finance/blue/chips");

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/search/ticker/{ticker}")
    public ResponseEntity<Object> symbolLookup(@PathVariable String ticker) {
        log.info(" > > > GET /api/v1/finance/search/ticker/{}", ticker);

        SearchResponse response = financeService.lookupForStockByTicker(ticker);

        log.info(" < < < GET /api/v1/finance/search/ticker/{}", ticker);

        return ResponseEntity.ok().body(response);
    }

}
