package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.entity.GameEntity;
import com.mcdlcn.squaregamesapi.entity.GamePlayerEntity;
import com.mcdlcn.squaregamesapi.entity.GameTokenEntity;
import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import com.mcdlcn.squaregamesapi.repository.GameEntityRepository;
import com.mcdlcn.squaregamesapi.service.StoredGame;
import fr.le_campus_numerique.square_games.engine.Game;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.Token;
import fr.le_campus_numerique.square_games.engine.TokenPosition;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;


@Primary
@Repository
public class JpaGameDao implements GameDao {
    private static final String TOKEN_LOCATION_BOARD = "BOARD";
    private static final String TOKEN_LOCATION_REMAINING = "REMAINING";
    private static final String TOKEN_LOCATION_REMOVED = "REMOVED";

    private final GameEntityRepository repository;
    private final List<GamePlugin> plugins;

    public JpaGameDao(GameEntityRepository repository, List<GamePlugin> plugins) {
        this.repository = repository;
        this.plugins = plugins;
    }

    @Override
    public Stream<StoredGame> findAll() {
        return repository.findAll().stream()
                .map(entity -> {
                    try {
                        return toStoredGame(entity);
                    } catch (InconsistentGameDefinitionException exception) {
                        throw new IllegalStateException("Could not restore game from database", exception);
                    }
                });
    }

    @Override
    public Optional<StoredGame> findById(UUID gameId) throws InconsistentGameDefinitionException {
        return repository.findById(gameId)
                .map(entity -> {
                    try {
                        return toStoredGame(entity);
                    } catch (InconsistentGameDefinitionException exception) {
                        throw new IllegalStateException("Could not restore game from database", exception);
                    }
                });
    }

    @Override
    @Transactional
    public StoredGame save(UUID gameId, StoredGame storedGame) {
        GameEntity entity = repository.findById(gameId).orElseGet(GameEntity::new);

        updateEntity(entity, gameId, storedGame);
        repository.save(entity);

        return storedGame;
    }

    @Override
    public void delete(UUID gameId) {
        repository.deleteById(gameId);
    }

    private void updateEntity(GameEntity entity, UUID gameId, StoredGame storedGame) {
        Game game = storedGame.game();

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