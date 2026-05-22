package com.mcdlcn.squaregamesapi.plugin;

import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.GameFactory;
import fr.le_campus_numerique.square_games.engine.tictactoe.TicTacToeGameFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class TicTacToePlugin implements GamePlugin {

    private final TicTacToeGameFactory ticTacToeGameFactory;
    private final MessageSource messageSource;


    @Value("${game.tictactoe.default-player-count}")
    private int defaultPlayerCount;

    @Value("${game.tictactoe.default-board-size}")
    private int defaultBoardSize;

    public TicTacToePlugin(
            TicTacToeGameFactory ticTacToeGameFactory,
            MessageSource messageSource,
            @Value("${game.tictactoe.default-player-count}") int defaultPlayerCount,
            @Value("${game.tictactoe.default-board-size}") int defaultBoardSize
    ) {
        this.ticTacToeGameFactory = ticTacToeGameFactory;
        this.messageSource = messageSource;
        this.defaultPlayerCount = defaultPlayerCount;
        this.defaultBoardSize = defaultBoardSize;
    }

    @Override
    public String getId() {
        return "tictactoe";
    }

    @Override
    public GameFactory getFactory() {
        return ticTacToeGameFactory;
    }

    @Override
    public String getName(Locale locale) {
        return messageSource.getMessage("game.tictactoe.name", null, locale);
    }

    @Override
    public Game createGame(Integer playerCount, Integer boardSize) {
        int finalPlayerCount = playerCount != null ? playerCount : defaultPlayerCount;
        int finalBoardSize = boardSize != null ? boardSize : defaultBoardSize;

        return ticTacToeGameFactory.createGame(finalPlayerCount, finalBoardSize);
    }
}