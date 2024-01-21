package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.entity.Faq;
import com.moneta.hub.moneta.model.message.request.FaqRequest;
import com.moneta.hub.moneta.model.message.response.FaqResponse;
import com.moneta.hub.moneta.repository.FaqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/v1/faq")
@RequiredArgsConstructor
@Slf4j
public class FaqController {

    private final FaqRepository faqRepository;

    @GetMapping
    public ResponseEntity<Object> getFAQs() {

        log.info(" > > > GET /api/v1/faq");
        List<Faq> faqList = faqRepository.findAll();
        log.info(" < < < GET /api/v1/faq");

        return ResponseEntity.ok().body(faqList.stream().map(FaqResponse::mapEntityToResponse));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Object> createFAQ(@Validated @RequestBody FaqRequest request) {
        log.info(" > > > POST /api/v1/faq");

        Faq faq = faqRepository.save(Faq.builder()
                                        .question(request.getQuestion())
                                        .answer(request.getAnswer())
                                        .build());
        log.info(" < < < POST /api/v1/faq");

        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                                                                 .path("/{id}")
                                                                 .buildAndExpand(faq.getId())
                                                                 .toUri())
                             .build();
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Object> deleteFAQ(@PathVariable Long id) {
        log.info(" > > > DELETE /api/v1/faq/{}", id);
        faqRepository.deleteById(id);
        log.info(" < < < DELETE /api/v1/faq/{}", id);
        return ResponseEntity.noContent().build();
    }
}
