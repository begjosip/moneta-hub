package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

import java.util.List;

@Data
public class AggregatesResponse {

    private String ticker;

    private Long queryCount;

    private Long resultsCount;

    private String status;

    private List<QuoteResponse> results;
}
