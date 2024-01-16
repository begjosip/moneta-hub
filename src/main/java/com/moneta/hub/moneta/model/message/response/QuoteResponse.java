package com.moneta.hub.moneta.model.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class QuoteResponse {

    private String companyName;

    private String ticker;

    @JsonProperty("c")
    private Double closingPrice;

    @JsonProperty("h")
    private Double highPrice;

    @JsonProperty("l")
    private Double lowPrice;

    @JsonProperty("d")
    private Double change;

    @JsonProperty("dp")
    private Double percentChange;

    @JsonProperty("o")
    private Double openingPrice;

    @JsonProperty("pc")
    private Double previousClose;

    @JsonProperty("t")
    private Long timestamp;
}
