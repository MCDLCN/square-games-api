package com.mcdlcn.squaregamesapi.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.connectfour.ConnectFourGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class ConnectFourPlugin implements GamePlugin {

    private final ConnectFourGameFactory connectFourGameFactory;
    private final MessageSource messageSource;
    private final int defaultPlayerCount;
    private final int defaultBoardSize;

    public ConnectFourPlugin(
            ConnectFourGameFactory connectFourGameFactory,
            MessageSource messageSource,
            @Value("${game.connectfour.default-player-count}") int defaultPlayerCount,
            @Value("${game.connectfour.default-board-size}") int defaultBoardSize
    ) {
        this.connectFourGameFactory = connectFourGameFactory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return "connectfour";
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int finalPlayerCount = playerCount != null ? playerCount : defaultPlayerCount;
        int finalBoardSize = boardSize != null ? boardSize : defaultBoardSize;

        return connectFourGameFactory.createGame(finalPlayerCount, finalBoardSize);
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.connectfour.name", null, locale);
    }

    @Override
    public GameFactory getFactory() {
        return this.connectFourGameFactory;
    }
}