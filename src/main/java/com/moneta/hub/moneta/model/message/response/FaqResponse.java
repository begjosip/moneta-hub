package com.moneta.hub.moneta.model.message.response;

import com.moneta.hub.moneta.model.entity.Faq;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FaqResponse {

    private Long id;

    private String question;

    private String answer;

    public static FaqResponse mapEntityToResponse(Faq faq) {
        return FaqResponse.builder()
                          .id(faq.getId())
                          .answer(faq.getAnswer())
                          .question(faq.getQuestion())
                          .build();
    }
}
