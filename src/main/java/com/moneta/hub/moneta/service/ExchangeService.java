package com.moneta.hub.moneta.service;

import com.moneta.hub.moneta.model.entity.Currency;
import com.moneta.hub.moneta.repository.CurrencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private final CurrencyRepository currencyRepository;

    public List<Currency> getAllCurrencies() {
        log.debug("Fetching all currencies from database.");
        return currencyRepository.findAll();
    }
}
