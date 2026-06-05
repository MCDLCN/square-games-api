package com.mcdlcn.squaregamesapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GameInfo(String id, @Schema(example = "tictactoe") String name) {
}
