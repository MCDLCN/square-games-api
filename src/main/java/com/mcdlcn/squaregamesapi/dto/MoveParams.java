package com.mcdlcn.squaregamesapi.dto;

import jakarta.validation.constraints.Min;

public record MoveParams(
        @Min(0) int x,
        @Min(0) int y
) {
}