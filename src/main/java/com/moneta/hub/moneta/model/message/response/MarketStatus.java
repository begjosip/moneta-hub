package com.moneta.hub.moneta.model.message.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MarketStatus {

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("holiday")
    private String holiday;

    @JsonProperty("isOpen")
    private boolean isOpen;

    @JsonProperty("session")
    private String session;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("t")
    private Long timestamp;
}
