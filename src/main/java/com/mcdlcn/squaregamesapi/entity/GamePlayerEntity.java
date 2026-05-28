package com.mcdlcn.squaregamesapi.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;

import java.util.UUID;

@Entity
@Table(name = "game_players")
public class GamePlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    public GameEntity game;

    public UUID playerId;

    public int playerOrder;
}
