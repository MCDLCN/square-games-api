package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameInfo;

import java.util.Collection;
import java.util.Locale;

public interface GameCatalog {
    Collection<String> getGameIdentifiers();
    Collection<GameInfo> getGames(Locale locale);
}