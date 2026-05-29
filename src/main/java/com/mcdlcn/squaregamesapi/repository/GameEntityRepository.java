package com.mcdlcn.squaregamesapi.repository;

import com.mcdlcn.squaregamesapi.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {
}