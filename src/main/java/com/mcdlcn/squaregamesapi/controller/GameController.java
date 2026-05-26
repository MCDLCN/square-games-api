package com.mcdlcn.squaregamesapi.controller;

import com.mcdlcn.squaregamesapi.dto.GameCreationParams;
import com.mcdlcn.squaregamesapi.dto.GameStateDto;
import com.mcdlcn.squaregamesapi.dto.MoveParams;
import com.mcdlcn.squaregamesapi.dto.PositionDto;
import com.mcdlcn.squaregamesapi.service.GameService;
import fr.le_campus_numerique.square_games.engine.InconsistentGameDefinitionException;
import fr.le_campus_numerique.square_games.engine.InvalidPositionException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping("/games")
    public UUID createGame(@Valid @RequestBody GameCreationParams params) {
        return gameService.createGame(params);
    }

    @GetMapping("/games/{gameId}")
    public Object getGame(@PathVariable UUID gameId) throws InconsistentGameDefinitionException {
        return gameService.getGame(gameId);
    }

    @GetMapping("/games/{gameId}/tokens/{tokenName}/moves")
    public Collection<PositionDto> getPossibleMoves(
            @PathVariable UUID gameId,
            @PathVariable String tokenName
    ) throws InconsistentGameDefinitionException {
        return gameService.getPossibleMoves(gameId, tokenName);
    }


    @PostMapping("/games/{gameId}/tokens/{tokenName}/moves")
    public GameStateDto playMove(
            @PathVariable UUID gameId,
            @PathVariable String tokenName,
            @RequestBody @Valid MoveParams moveParams
    ) throws InvalidPositionException, InconsistentGameDefinitionException {
        return gameService.playMove(gameId, tokenName, moveParams);
    }
}