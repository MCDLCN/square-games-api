package com.mcdlcn.squaregamesapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.UUID;

public record GameCreationParams(
        @NotBlank String gameType,
        @Min(1) Integer playerCount,
        @Min(1) Integer boardSize,
        List<UUID> opponentIds
) {
}