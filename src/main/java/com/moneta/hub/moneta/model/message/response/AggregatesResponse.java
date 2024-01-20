package com.moneta.hub.moneta.model.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AggregatesResponse {

    private String ticker;

    private Long queryCount;

    private Long resultsCount;

    private String status;

    @JsonProperty("next_url")
    private String nextUrl;

    private List<QuoteResponse> results;

    public void addQuoteResponse(List<QuoteResponse> quoteResponses) {
        results.addAll(quoteResponses);
    }
}
