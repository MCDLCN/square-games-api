package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.service.StoredGame;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GameDao {

    Collection<StoredGame> findAll() throws InconsistentGameDefinitionException;

    Optional<StoredGame> findById(UUID gameId) throws InconsistentGameDefinitionException;

    StoredGame save(UUID gameId, StoredGame storedGame);

    void delete(UUID gameId);
}
