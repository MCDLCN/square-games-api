package com.mcdlcn.squaregamesapi.dto;

import fr.le_campus_numerique.square_games.engine.Game;

import java.util.List;
import java.util.UUID;

public record GameStateDto(
        UUID id,
        String gameType,
        int boardSize,
        UUID currentPlayerId,
        String status,
        List<BoardTokenDto> board,
        List<TokenDto> remainingTokens,
        List<TokenDto> removedTokens
) {

    public static GameStateDto fromGame(Game game, String gameType) {
        return new GameStateDto(
                game.getId(),
                gameType,
                game.getBoardSize(),
                game.getCurrentPlayerId(),
                game.getStatus().toString(),
                game.getBoard().entrySet().stream()
                        .map(entry -> new BoardTokenDto(
                                new PositionDto(
                                        entry.getKey().x(),
                                        entry.getKey().y()
                                ),
                                TokenDto.fromToken(entry.getValue())
                        ))
                        .toList(),
                game.getRemainingTokens().stream()
                        .map(TokenDto::fromToken)
                        .toList(),

                game.getRemovedTokens().stream()
                        .map(TokenDto::fromToken)
                        .toList()
        );
    }
}