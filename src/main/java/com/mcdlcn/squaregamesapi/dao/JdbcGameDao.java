package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.plugin.GamePlugin;
import com.mcdlcn.squaregamesapi.service.StoredGame;
import fr.le_campus_numerique.square_games.engine.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;

import java.util.*;
import java.util.stream.Stream;

//@Repository
public class JdbcGameDao implements GameDao {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final List<GamePlugin> plugins;

    public JdbcGameDao(
            NamedParameterJdbcTemplate jdbcTemplate,
            List<GamePlugin> plugins
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.plugins = plugins;
    }

    @Override
    public Stream<StoredGame> findAll() {
        return Stream.empty();
    }

    @Override
    public Optional<StoredGame> findById(UUID gameId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                """
                SELECT *
                FROM games
                WHERE id = :id
                """,
                Map.of("id", gameId)
        );

        if (rows.isEmpty()) {
            return Optional.empty();
        }

        Map<String, Object> row = rows.getFirst();

        String gameType = (String) row.get("game_type");
        int boardSize = (Integer) row.get("board_size");

        List<UUID> playerIds = jdbcTemplate.query(
                """
                SELECT player_id
                FROM game_players
                WHERE game_id = :gameId
                ORDER BY player_order
                """,
                Map.of("gameId", gameId),
                (rs, rowNum) -> rs.getObject("player_id", UUID.class)
        );

        List<Map<String, Object>> tokenRows = jdbcTemplate.queryForList(
                """
                SELECT *
                FROM game_tokens
                WHERE game_id = :gameId
                ORDER BY id
                """,
                Map.of("gameId", gameId)
        );

        List<TokenPosition<UUID>> placedTokens = new ArrayList<>();
        List<TokenPosition<UUID>> removedTokens = new ArrayList<>();

        for (Map<String, Object> tokenRow : tokenRows) {
            UUID ownerId = (UUID) tokenRow.get("owner_id");
            String tokenName = (String) tokenRow.get("name");
            boolean removed = (Boolean) tokenRow.get("removed");
            Integer x = (Integer) tokenRow.get("x");
            Integer y = (Integer) tokenRow.get("y");

            if (x == null || y == null) {
                continue;
            }

            TokenPosition<UUID> tokenPosition = new TokenPosition<>(
                    ownerId,
                    tokenName,
                    x,
                    y
            );

            if (removed) {
                removedTokens.add(tokenPosition);
            } else {
                placedTokens.add(tokenPosition);
            }
        }

        GamePlugin plugin = findPlugin(gameType);

        try {
            Game game = plugin.getFactory().createGameWithIds(
                    gameId,
                    boardSize,
                    playerIds,
                    placedTokens,
                    removedTokens
            );

            return Optional.of(new StoredGame(gameType, game));
        } catch (InconsistentGameDefinitionException e) {
            throw new IllegalStateException("Could not restore game from database", e);
        }
    }

    @Override
    public StoredGame save(UUID gameId, StoredGame storedGame) {
        jdbcTemplate.update(
                """
                INSERT INTO games (id, game_type, board_size, current_player_id, status)
                VALUES (:id, :gameType, :boardSize, :currentPlayerId, :status)
                ON CONFLICT (id)
                DO UPDATE SET
                    game_type = EXCLUDED.game_type,
                    board_size = EXCLUDED.board_size,
                    current_player_id = EXCLUDED.current_player_id,
                    status = EXCLUDED.status
                """,
                Map.of(
                        "id", gameId,
                        "gameType", storedGame.gameType(),
                        "boardSize", storedGame.game().getBoardSize(),
                        "currentPlayerId", storedGame.game().getCurrentPlayerId(),
                        "status", storedGame.game().getStatus().toString()
                )
        );

        jdbcTemplate.update(
                "DELETE FROM game_players WHERE game_id = :gameId",
                Map.of("gameId", gameId)
        );

        int order = 0;
        for (UUID playerId : storedGame.game().getPlayerIds()) {
            jdbcTemplate.update(
                    """
                    INSERT INTO game_players (game_id, player_id, player_order)
                    VALUES (:gameId, :playerId, :playerOrder)
                    """,
                    Map.of(
                            "gameId", gameId,
                            "playerId", playerId,
                            "playerOrder", order++
                    )
            );
        }

        saveTokens(gameId, storedGame);

        return storedGame;
    }

    @Override
    public void delete(UUID gameId) {
        jdbcTemplate.update(
                "DELETE FROM games WHERE id = :id",
                Map.of("id", gameId)
        );
    }

    private void saveTokens(UUID gameId, StoredGame storedGame) {
        jdbcTemplate.update(
                "DELETE FROM game_tokens WHERE game_id = :gameId",
                Map.of("gameId", gameId)
        );

        storedGame.game().getRemainingTokens().forEach(token -> saveToken(gameId, token, false));
        storedGame.game().getRemovedTokens().forEach(token -> saveToken(gameId, token, true));
        storedGame.game().getBoard().values().forEach(token -> saveToken(gameId, token, false));
    }
    private void saveToken(UUID gameId, Token token, boolean removed) {
        Map<String, Object> params = new HashMap<>();

        params.put("gameId", gameId);
        params.put("name", token.getName());
        params.put("ownerId", token.getOwnerId().orElse(null));
        params.put("removed", removed);
        params.put("x", token.getPosition() != null ? token.getPosition().x() : null);
        params.put("y", token.getPosition() != null ? token.getPosition().y() : null);

        jdbcTemplate.update(
                """
                INSERT INTO game_tokens (game_id, name, owner_id, removed, x, y)
                VALUES (:gameId, :name, :ownerId, :removed, :x, :y)
                """,
                params
        );
    }

    private GamePlugin findPlugin(String gameType) {
        return plugins.stream()
                .filter(plugin -> plugin.getId().equals(gameType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown game type"));
    }
}