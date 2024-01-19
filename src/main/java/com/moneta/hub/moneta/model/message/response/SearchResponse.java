package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponse {

    private Long count;

    private List<StockSearchResult> result;
}
