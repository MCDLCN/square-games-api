package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import com.mcdlcn.squaregamesapi.exception.GameNotFoundException;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.*;
import org.springframework.stereotype.Service;
import com.mcdlcn.squaregamesapi.dao.GameDao;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private final GameDao gameDao;
    private final List<GamePlugin> plugins;

    public GameServiceImpl(List<GamePlugin> plugins, GameDao gameDao) {
        this.plugins = plugins;
        this.gameDao = gameDao;
    }

    @Override
    public UUID createGame(GameCreationParams params) {
        GamePlugin plugin = plugins.stream()
                .filter(p -> p.getId().equals(params.gameType()))
                .findFirst()
                .orElseThrow();

        Game game = plugin.createGame(params.playerCount(), params.boardSize());

        UUID id = UUID.randomUUID();
        gameDao.save(id, new StoredGame(plugin.getId(), game));

        return id;
    }

    @Override
    public GameStateDto getGame(UUID gameId) throws InconsistentGameDefinitionException {
        StoredGame storedGame = gameDao.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        return GameStateDto.fromGame(
                gameId,
                storedGame.game(),
                storedGame.gameType()
        );
    }

    @Override
    public Collection<PositionDto> getPossibleMoves(UUID gameId, String tokenName) throws InconsistentGameDefinitionException {
        StoredGame storedGame = gameDao.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        return storedGame.game()
                .getRemainingTokens().stream()
                .filter(token -> token.getName().equals(tokenName))
                .findFirst()
                .orElseThrow()
                .getAllowedMoves().stream()
                .map(position -> new PositionDto(
                        position.x(),
                        position.y()
                ))
                .toList();
    }

    @Override
    public GameStateDto playMove(UUID gameId, String tokenName, MoveParams moveParams)
            throws InvalidPositionException, InconsistentGameDefinitionException {
        StoredGame storedGame = gameDao.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        Game game = storedGame.game();

        Token token = game.getRemainingTokens().stream()
                .filter(t -> t.getName().equals(tokenName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        CellPosition targetPosition = new CellPosition(
                moveParams.x(),
                moveParams.y()
        );

        token.moveTo(targetPosition);

        gameDao.save(gameId, storedGame);

        return GameStateDto.fromGame(
                gameId,
                game,
                storedGame.gameType()
        );
    }
}