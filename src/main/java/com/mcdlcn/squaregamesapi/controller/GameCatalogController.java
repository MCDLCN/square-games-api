package com.mcdlcn.squaregamesapi.controller;

import com.mcdlcn.squaregamesapi.dto.GameInfo;
import com.mcdlcn.squaregamesapi.service.GameCatalog;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Locale;

@RestController
public class GameCatalogController {

    private final GameCatalog gameCatalog;

    public GameCatalogController(GameCatalog gameCatalog) {
        this.gameCatalog = gameCatalog;
    }

    @GetMapping("/games")
    public Collection<GameInfo> getGames(Locale locale) {
        return gameCatalog.getGames(locale);
    }
}