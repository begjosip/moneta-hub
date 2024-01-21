package com.moneta.hub.moneta.controller;

import com.moneta.hub.moneta.model.message.response.QuoteResponse;
import com.moneta.hub.moneta.service.UserService;
import com.moneta.hub.moneta.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/user/stock")
@RequiredArgsConstructor
@Slf4j
public class StockController {

    private final UserService userService;

    @PostMapping("/{ticker}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<String> addStockToUsersFavourites(@PathVariable String ticker,
                                                            @NotNull HttpServletRequest httpServletRequest) {
        log.info(" > > > POST /api/v1/user/stock/{}", ticker);
        userService.addStockToUsersFavourites(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), ticker);
        log.info(" < < < POST /api/v1/user/stock/{}", ticker);

        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> getAllUserStocks(@NotNull HttpServletRequest httpServletRequest) {
        log.info(" > > > GET /api/v1/user/stock");
        List<QuoteResponse> userStocks = userService.getAllUserFavouriteStocks(
                SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest));
        log.info(" < < < GET /api/v1/user/stock");

        return ResponseEntity.ok().body(userStocks);
    }

    @DeleteMapping("/{ticker}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Object> removeStockFromUserFavourites(@NotNull HttpServletRequest httpServletRequest,
                                                                @PathVariable String ticker) {
        log.info(" > > > DELETE /api/v1/user/stock/{}", ticker);
        userService.deleteStockFromUserFavourites(SecurityUtil.getBearerTokenFromHttpRequest(httpServletRequest), ticker);
        log.info(" < < < DELETE /api/v1/user/stock/{}", ticker);

        return ResponseEntity.noContent().build();
    }
}
