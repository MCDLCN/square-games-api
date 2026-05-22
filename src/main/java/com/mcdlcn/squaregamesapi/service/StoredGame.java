package com.mcdlcn.squaregamesapi.service;

import fr.le_campus_numerique.square_games.engine.Game;

public record StoredGame(
        String gameType,
        Game game
) {
}