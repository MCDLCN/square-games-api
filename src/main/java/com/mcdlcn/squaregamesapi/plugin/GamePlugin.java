package com.mcdlcn.squaregamesapi.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;

import java.util.Locale;

public interface GamePlugin {
    String getId();

    String getName(Locale locale);

    public GameFactory getFactory();

    Game createGame(Integer playerCount, Integer boardSize);
}