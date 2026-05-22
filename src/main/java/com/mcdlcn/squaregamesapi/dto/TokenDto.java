package com.mcdlcn.squaregamesapi.dto;

import fr.le_campus_numerique.square_games.engine.Token;

import java.util.List;
import java.util.UUID;

public record TokenDto(
        String name,
        UUID ownerId,
        PositionDto position,
        List<PositionDto> allowedMoves
) {

    public static TokenDto fromToken(Token token) {
        return new TokenDto(
                token.getName(),
                token.getOwnerId().orElse(null),
                token.getPosition() == null
                        ? null
                        : new PositionDto(
                        token.getPosition().x(),
                        token.getPosition().y()
                ),
                token.getAllowedMoves().stream()
                        .map(position -> new PositionDto(position.x(), position.y()))
                        .toList()
        );
    }
}