package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.exception.GameNotFoundException;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final Map<UUID, StoredGame> games = new HashMap<>();
    private final List<GamePlugin> plugins;

    public GameServiceImpl(List<GamePlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public UUID createGame(GameCreationParams params) {
        GamePlugin plugin = plugins.stream()
                .filter(p -> p.getId().equals(params.gameType()))
                .findFirst()
                .orElseThrow();

        Game game = plugin.createGame(params.playerCount(), params.boardSize());

        UUID id = UUID.randomUUID();
        games.put(id, new StoredGame(plugin.getId(), game));

        return id;
    }

    @Override
    public GameStateDto getGame(UUID gameId) {
        StoredGame storedGame = games.get(gameId);

        if (storedGame == null) {
            throw new GameNotFoundException(gameId);
        }

        return GameStateDto.fromGame(storedGame.game(), storedGame.gameType());
    }

    @Override
    public Collection<String> getPossibleMoves(UUID gameId, String tokenId) {
        return List.of();
    }

    @Override
    public GameStateDto playMove(UUID gameId, String tokenName, MoveParams moveParams)
            throws InvalidPositionException {
        StoredGame storedGame = games.get(gameId);

        if (storedGame == null) {
            throw new GameNotFoundException(gameId);
        }

        Game game = storedGame.game();

        Token token = game.getRemainingTokens().stream()
                .filter(t -> t.getName().equals(tokenName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        CellPosition targetPosition = new CellPosition(moveParams.x(), moveParams.y());

        if (!token.getAllowedMoves().contains(targetPosition)) {
            throw new IllegalArgumentException("Move is not allowed");
        }

        token.moveTo(targetPosition);

        return GameStateDto.fromGame(game, storedGame.gameType());
    }
}