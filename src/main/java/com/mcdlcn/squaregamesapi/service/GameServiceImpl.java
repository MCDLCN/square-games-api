package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dao.GameDao;
import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import com.mcdlcn.squaregamesapi.exception.ForbiddenActionException;
import com.mcdlcn.squaregamesapi.exception.GameNotFoundException;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import fr.le_campus_numerique.square_games.engine.CellPosition;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import fr.le_campus_numerique.square_games.engine.Token;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final GameDao gameDao;
    private final List<GamePlugin> plugins;

    public GameServiceImpl(
            GameDao gameDao,
            List<GamePlugin> plugins
    ) {
        this.gameDao = gameDao;
        this.plugins = plugins;
    }

    @Override
    public UUID createGame(UUID userId, GameCreationParams params) throws InconsistentGameDefinitionException {

        GamePlugin plugin = findPlugin(params.gameType());

        List<UUID> playerIds = new ArrayList<>();
        playerIds.add(userId);

        if (params.opponentIds() != null) {
            playerIds.addAll(params.opponentIds());
        }

        if (playerIds.size() != params.playerCount()) {
            throw new IllegalArgumentException("Player count does not match the provided users");
        }

        UUID gameId = UUID.randomUUID();

        Game game = plugin.getFactory().createGameWithIds(
                gameId,
                params.boardSize(),
                playerIds,
                List.of(),
                List.of()
        );

        gameDao.save(gameId, new StoredGame(plugin.getId(), game));

        return gameId;
    }

    @Override
    public GameStateDto getGame(UUID gameId) throws InconsistentGameDefinitionException {
        StoredGame storedGame = getStoredGame(gameId);

        return GameStateDto.fromGame(
                gameId,
                storedGame.game(),
                storedGame.gameType()
        );
    }

    @Override
    public Collection<PositionDto> getPossibleMoves(UUID gameId, String tokenName) throws InconsistentGameDefinitionException {
        StoredGame storedGame = getStoredGame(gameId);

        for (Token token : storedGame.game().getRemainingTokens()) {
            if (token.getName().equals(tokenName)) {
                List<PositionDto> positions = new ArrayList<>();

                for (CellPosition position : token.getAllowedMoves()) {
                    positions.add(new PositionDto(position.x(), position.y()));
                }

                return positions;
            }
        }

        throw new IllegalArgumentException("Token not found");
    }

    @Override
    public GameStateDto playMove(
            UUID userId,
            UUID gameId,
            String tokenName,
            MoveParams moveParams
    ) throws InvalidPositionException, InconsistentGameDefinitionException {

        StoredGame storedGame = getStoredGame(gameId);
        Game game = storedGame.game();

        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new ForbiddenActionException("It is not this user's turn");
        }

        Token token = findRemainingToken(game, tokenName);
        CellPosition targetPosition = new CellPosition(moveParams.x(), moveParams.y());

        token.moveTo(targetPosition);

        gameDao.save(gameId, storedGame);

        return GameStateDto.fromGame(
                gameId,
                game,
                storedGame.gameType()
        );
    }

    @Override
    public Collection<GameStateDto> getGames(UUID userId) throws InconsistentGameDefinitionException {

        List<GameStateDto> games = new ArrayList<>();

        for (StoredGame storedGame : gameDao.findAll()) {
            if (storedGame.game().getPlayerIds().contains(userId)) {
                games.add(GameStateDto.fromGame(
                        storedGame.game().getId(),
                        storedGame.game(),
                        storedGame.gameType()
                ));
            }
        }

        return games;
    }

    private StoredGame getStoredGame(UUID gameId) throws InconsistentGameDefinitionException {
        return gameDao.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
    }

    private Token findRemainingToken(Game game, String tokenName) {
        for (Token token : game.getRemainingTokens()) {
            if (token.getName().equals(tokenName)) {
                return token;
            }
        }

        throw new IllegalArgumentException("Token not found");
    }

    private GamePlugin findPlugin(String gameType) {
        for (GamePlugin plugin : plugins) {
            if (plugin.getId().equals(gameType)) {
                return plugin;
            }
        }

        throw new IllegalArgumentException("Unknown game type: " + gameType);
    }
}
