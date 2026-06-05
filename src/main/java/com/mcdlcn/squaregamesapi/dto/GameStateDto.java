package com.mcdlcn.squaregamesapi.dto;

import fr.le_campus_numerique.square_games.engine.Game;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record GameStateDto(
        @Schema(example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,
        String gameType,
        int boardSize,
        UUID currentPlayerId,
        String status,
        List<BoardTokenDto> board,
        List<TokenDto> remainingTokens,
        List<TokenDto> removedTokens
) {

    public static GameStateDto fromGame(UUID id, Game game, String gameType) {
        return new GameStateDto(
                id,
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