package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;

import java.util.Collection;
import java.util.UUID;

public interface GameService {
    UUID createGame(GameCreationParams params);

    GameStateDto getGame(UUID gameId) throws InconsistentGameDefinitionException;

    Collection<PositionDto> getPossibleMoves(UUID gameId, String tokenName) throws InconsistentGameDefinitionException;

    GameStateDto playMove(UUID gameId, String tokenName, MoveParams moveParams) throws InvalidPositionException, InconsistentGameDefinitionException;

}