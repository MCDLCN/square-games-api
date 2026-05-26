package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.service.StoredGame;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface GameDao {

    Stream<StoredGame> findAll();

    Optional<StoredGame> findById(UUID gameId) throws InconsistentGameDefinitionException;

    StoredGame save(UUID gameId, StoredGame storedGame);

    void delete(UUID gameId);
}