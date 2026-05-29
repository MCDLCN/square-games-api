package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import com.mcdlcn.squaregamesapi.entity.GameEntity;
import com.mcdlcn.squaregamesapi.entity.GamePlayerEntity;
import com.mcdlcn.squaregamesapi.entity.GameTokenEntity;
import com.mcdlcn.squaregamesapi.exception.ForbiddenActionException;
import com.mcdlcn.squaregamesapi.exception.GameNotFoundException;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import com.mcdlcn.squaregamesapi.repository.GameEntityRepository;
import fr.le_campus_numerique.square_games.engine.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameServiceImpl implements GameService {

    private static final String TOKEN_LOCATION_BOARD = "BOARD";
    private static final String TOKEN_LOCATION_REMAINING = "REMAINING";
    private static final String TOKEN_LOCATION_REMOVED = "REMOVED";

    private final List<GamePlugin> plugins;
    private final UserValidationService userValidationService;
    private final GameEntityRepository gameRepository;

    public GameServiceImpl(
            List<GamePlugin> plugins,
            GameEntityRepository gameRepository,
            UserValidationService userValidationService
    ) {
        this.plugins = plugins;
        this.gameRepository = gameRepository;
        this.userValidationService = userValidationService;
    }

    @Override
    public UUID createGame(UUID userId, GameCreationParams params) throws InconsistentGameDefinitionException {
        userValidationService.validateUser(userId);

        GamePlugin plugin = findPlugin(params.gameType());

        List<UUID> playerIds = new ArrayList<>();
        playerIds.add(userId);

        if (params.opponentIds() != null) {
            for (UUID opponentId : params.opponentIds()) {
                userValidationService.validateUser(opponentId);
                playerIds.add(opponentId);
            }
        }

        if (playerIds.size() != params.playerCount()) {
            throw new IllegalArgumentException(
                    "Player count does not match the provided users"
            );
        }

        UUID gameId = UUID.randomUUID();

        Game game = plugin.getFactory().createGameWithIds(
                gameId,
                params.boardSize(),
                playerIds,
                List.of(),
                List.of()
        );

        GameEntity entity = toEntity(
                gameId,
                new StoredGame(plugin.getId(), game)
        );

        gameRepository.save(entity);

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

        return storedGame.game()
                .getRemainingTokens().stream()
                .filter(token -> token.getName().equals(tokenName))
                .findFirst()
                .orElseThrow()
                .getAllowedMoves().stream()
                .map(position -> new PositionDto(position.x(), position.y()))
                .toList();
    }

    @Override
    public GameStateDto playMove(
            UUID userId,
            UUID gameId,
            String tokenName,
            MoveParams moveParams
    ) throws InvalidPositionException, InconsistentGameDefinitionException {
        userValidationService.validateUser(userId);

        StoredGame storedGame = getStoredGame(gameId);
        Game game = storedGame.game();

        if (!userId.equals(game.getCurrentPlayerId())) {
            throw new ForbiddenActionException("It is not this user's turn");
        }

        Token token = game.getRemainingTokens().stream()
                .filter(t -> t.getName().equals(tokenName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Token not found"));

        CellPosition targetPosition = new CellPosition(moveParams.x(), moveParams.y());

        token.moveTo(targetPosition);

        gameRepository.save(toEntity(gameId, storedGame));

        return GameStateDto.fromGame(
                gameId,
                game,
                storedGame.gameType()
        );
    }

    @Override
    public Collection<GameStateDto> getGames(UUID userId) throws InconsistentGameDefinitionException {
        userValidationService.validateUser(userId);

        List<GameStateDto> games = new ArrayList<>();

        for (GameEntity entity : gameRepository.findAll()) {
            StoredGame storedGame = toStoredGame(entity);

            if (storedGame.game().getPlayerIds().contains(userId)) {
                games.add(GameStateDto.fromGame(
                        entity.id,
                        storedGame.game(),
                        storedGame.gameType()
                ));
            }
        }

        return games;
    }

    private StoredGame getStoredGame(UUID gameId) throws InconsistentGameDefinitionException {
        GameEntity entity = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        return toStoredGame(entity);
    }

    private GameEntity toEntity(UUID gameId, StoredGame storedGame) {
        Game game = storedGame.game();

        GameEntity entity = gameRepository.findById(gameId).orElseGet(GameEntity::new);

        entity.id = gameId;
        entity.gameType = storedGame.gameType();
        entity.engineGameType = game.getFactoryId();
        entity.boardSize = game.getBoardSize();
        entity.currentPlayerId = game.getCurrentPlayerId();
        entity.status = game.getStatus().name();

        if (entity.players == null) {
            entity.players = new ArrayList<>();
        }

        if (entity.tokens == null) {
            entity.tokens = new ArrayList<>();
        }

        entity.players.clear();
        entity.tokens.clear();

        int playerOrder = 0;
        for (UUID playerId : game.getPlayerIds()) {
            GamePlayerEntity playerEntity = new GamePlayerEntity();

            playerEntity.game = entity;
            playerEntity.playerId = playerId;
            playerEntity.playerOrder = playerOrder++;

            entity.players.add(playerEntity);
        }

        for (Token token : game.getRemainingTokens()) {
            entity.tokens.add(toTokenEntity(entity, token, TOKEN_LOCATION_REMAINING));
        }

        for (Token token : game.getRemovedTokens()) {
            entity.tokens.add(toTokenEntity(entity, token, TOKEN_LOCATION_REMOVED));
        }

        for (Token token : game.getBoard().values()) {
            entity.tokens.add(toTokenEntity(entity, token, TOKEN_LOCATION_BOARD));
        }

        return entity;
    }

    private GameTokenEntity toTokenEntity(GameEntity gameEntity, Token token, String tokenLocation) {
        GameTokenEntity entity = new GameTokenEntity();

        entity.game = gameEntity;
        entity.name = token.getName();
        entity.ownerId = token.getOwnerId().orElse(null);
        entity.removed = TOKEN_LOCATION_REMOVED.equals(tokenLocation);
        entity.tokenLocation = tokenLocation;

        if (token.getPosition() != null) {
            entity.x = token.getPosition().x();
            entity.y = token.getPosition().y();
        }

        return entity;
    }

    private StoredGame toStoredGame(GameEntity entity) throws InconsistentGameDefinitionException {
        List<UUID> playerIds = entity.players.stream()
                .sorted(Comparator.comparingInt(player -> player.playerOrder))
                .map(player -> player.playerId)
                .toList();

        List<TokenPosition<UUID>> placedTokens = new ArrayList<>();
        List<TokenPosition<UUID>> removedTokens = new ArrayList<>();

        for (GameTokenEntity token : entity.tokens) {
            if (token.x == null || token.y == null) {
                continue;
            }

            TokenPosition<UUID> tokenPosition = new TokenPosition<>(
                    token.ownerId,
                    token.name,
                    token.x,
                    token.y
            );

            if (TOKEN_LOCATION_REMOVED.equals(token.tokenLocation) || token.removed) {
                removedTokens.add(tokenPosition);
            } else if (TOKEN_LOCATION_BOARD.equals(token.tokenLocation)) {
                placedTokens.add(tokenPosition);
            }
        }

        GamePlugin plugin = findPlugin(entity.gameType);

        Game game = plugin.getFactory().createGameWithIds(
                entity.id,
                entity.boardSize,
                playerIds,
                placedTokens,
                removedTokens
        );

        return new StoredGame(entity.gameType, game);
    }

    private GamePlugin findPlugin(String gameType) {
        return plugins.stream()
                .filter(plugin -> plugin.getId().equals(gameType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown game type: " + gameType));
    }
}