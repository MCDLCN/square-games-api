package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameInfo;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Service
public class GameCatalogImpl implements GameCatalog {

    private final List<GamePlugin> plugins;

    public GameCatalogImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Collection<String> getGameIdentifiers() {
        return plugins.stream()
                .map(GamePlugin::getId)
                .toList();
    }

    @Override
    public Collection<GameInfo> getGames(Locale locale) {
        return plugins.stream()
                .map(plugin -> new GameInfo(plugin.getId(), plugin.getName(locale)))
                .toList();
    }
}