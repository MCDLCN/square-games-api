package com.mcdlcn.squaregamesapi.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "game_tokens")
public class GameTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    public GameEntity game;

    public String name;

    public UUID ownerId;

    public boolean removed;

    public Integer x;

    public Integer y;

    public String tokenLocation;
}