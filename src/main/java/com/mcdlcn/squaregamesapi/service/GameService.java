package com.mcdlcn.squaregamesapi.service;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;

import java.util.Collection;
import java.util.UUID;

public interface GameService {
    UUID createGame(GameCreationParams params);

    GameStateDto getGame(UUID gameId);

    Collection<String> getPossibleMoves(UUID gameId, String tokenId);

    GameStateDto playMove(UUID gameId, String tokenName, MoveParams moveParams) throws InvalidPositionException;

}