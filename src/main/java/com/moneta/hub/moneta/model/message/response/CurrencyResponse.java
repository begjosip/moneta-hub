package com.moneta.hub.moneta.model.message.response;

import com.moneta.hub.moneta.model.entity.Currency;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponse {

    private Long id;

    private String code;

    private String currencyName;

    private String countryCode;

    public static CurrencyResponse mapEntityToCurrencyResponse(Currency currency) {
        return CurrencyResponse.builder()
                               .id(currency.getId())
                               .code(currency.getCode())
                               .currencyName(currency.getCurrencyName())
                               .countryCode(currency.getCountryCode())
                               .build();
    }
}
