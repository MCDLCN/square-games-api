package com.mcdlcn.squaregamesapi.controller;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import com.mcdlcn.squaregamesapi.service.GameService;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@Tag(name = "Games", description = "Game management endpoints")
@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Create a new game")
    @PostMapping("/games")
    public UUID createGame(
            @Valid @RequestBody GameCreationParams params,
            Authentication authentication
    ) throws InconsistentGameDefinitionException {
        UUID userId = (UUID) authentication.getPrincipal();
        return gameService.createGame(userId, params);
    }

    @Operation(summary = "Get a game by its ID")
    @GetMapping("/games/{gameId}")
    public GameStateDto getGame(@PathVariable UUID gameId) throws InconsistentGameDefinitionException {
        return gameService.getGame(gameId);
    }

    @Operation(summary = "Get possible moves for a player")
    @GetMapping("/games/{gameId}/tokens/{tokenName}/moves")
    public Collection<PositionDto> getPossibleMoves(
            @PathVariable UUID gameId,
            @PathVariable String tokenName
    ) throws InconsistentGameDefinitionException {
        return gameService.getPossibleMoves(gameId, tokenName);
    }


    @Operation(summary = "Play a move for a token")
    @PostMapping("/games/{gameId}/tokens/{tokenName}/moves")
    public GameStateDto playMove(
            @PathVariable UUID gameId,
            @PathVariable String tokenName,
            @RequestBody @Valid MoveParams moveParams,
            Authentication authentication
    ) throws InvalidPositionException, InconsistentGameDefinitionException {
        UUID userId = (UUID) authentication.getPrincipal();
        return gameService.playMove(userId, gameId, tokenName, moveParams);
    }

    @GetMapping("/active")
    public Collection<GameStateDto> getGames(
            Authentication authentication
    ) throws InconsistentGameDefinitionException {
        UUID userId = (UUID) authentication.getPrincipal();
        return gameService.getGames(userId);
    }
}