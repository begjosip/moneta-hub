package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.message.response.CompanyProfileResponse;
import com.moneta.hub.moneta.model.message.response.NewsResponse;
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

    @GetMapping("/all/news")
    public ResponseEntity<Object> getStockMarketNews() {

        log.debug(" > > > GET /api/v1/finance/all/news");
        List<NewsResponse> response = financeService.fetchStockMarketNews();
        log.debug(" < < < GET /api/v1/finance/all/news");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/company/profile/{ticker}")
    public ResponseEntity<Object> getCompanyProfile(@PathVariable String ticker) {

        log.debug(" > > > GET /api/v1/finance/company/profile/{}", ticker);
        CompanyProfileResponse response = financeService.fetchCompanyProfile(ticker);
        log.debug(" < < < GET /api/v1/finance/company/profile/{}", ticker);

        return ResponseEntity.ok(response);
    }

}
