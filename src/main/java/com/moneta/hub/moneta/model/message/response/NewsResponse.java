package com.moneta.hub.moneta.model.message.response;

import lombok.Data;

@Data
public class NewsResponse {

    private String category;

    private Long datetime;

    private String headline;

    private Long id;

    private String image;

    private String source;

    private String summary;

    private String url;
}
