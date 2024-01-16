package com.moneta.hub.moneta.model.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class QuoteResponse {

    @JsonProperty("c")
    private Double closingPrice;

    @JsonProperty("h")
    private Double highPrice;

    @JsonProperty("l")
    private Double lowPrice;

    @JsonProperty("o")
    private Double openingPrice;

    @JsonProperty("pc")
    private Double previousClose;

    @JsonProperty("t")
    private Long timestamp;
}
