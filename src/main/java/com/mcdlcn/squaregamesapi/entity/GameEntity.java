package com.mcdlcn.squaregamesapi.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    public UUID id;

    public String gameType;
    public String engineGameType;

    public int boardSize;

    public UUID currentPlayerId;

    public String status;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("playerOrder ASC")
    public List<GamePlayerEntity> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<GameTokenEntity> tokens = new ArrayList<>();
}