package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

@Data
public class StockSearchResult {

    private String description;
    private String displaySymbol;
    private String symbol;
    private String type;
}
