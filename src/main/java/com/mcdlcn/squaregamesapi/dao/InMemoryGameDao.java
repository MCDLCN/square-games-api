package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.service.StoredGame;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

//@Repository
public class InMemoryGameDao implements GameDao {

    private final Map<UUID, StoredGame> games = new HashMap<>();

    @Override
    public Stream<StoredGame> findAll() {
        return games.values().stream();
    }

    @Override
    public Optional<StoredGame> findById(UUID gameId) {
        return Optional.ofNullable(games.get(gameId));
    }

    @Override
    public StoredGame save(UUID gameId, StoredGame storedGame) {
        games.put(gameId, storedGame);
        return storedGame;
    }

    @Override
    public void delete(UUID gameId) {
        games.remove(gameId);
    }
}