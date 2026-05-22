package com.mcdlcn.squaregamesapi.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.taquin.TaquinGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class TaquinPlugin implements GamePlugin {

    private final TaquinGameFactory taquinGameFactory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public TaquinPlugin(
            TaquinGameFactory taquinGameFactory,
            MessageSource messageSource,
            @Value("${game.taquin.default-player-count}") int defaultPlayerCount,
            @Value("${game.taquin.default-board-size}") int defaultBoardSize
    ) {
        this.taquinGameFactory = taquinGameFactory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return "taquin";
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int finalBoardSize = boardSize != null ? boardSize : defaultBoardSize;

        return taquinGameFactory.createGame(finalBoardSize, Set.of(UUID.randomUUID()));
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.taquin.name", null, locale);
    }

    @Override
    public GameFactory getFactory() {
        return this.taquinGameFactory;
    }
}