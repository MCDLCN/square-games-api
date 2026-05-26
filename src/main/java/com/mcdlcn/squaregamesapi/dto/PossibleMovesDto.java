package com.mcdlcn.squaregamesapi.dto;

import java.util.List;

public record PossibleMovesDto(
        String tokenName,
        List<PositionDto> moves
) {
}