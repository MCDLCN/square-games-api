package com.mcdlcn.squaregamesapi.repository;

import com.mcdlcn.squaregamesapi.entity.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Provides database access for persisted games.
 */
@Repository
public interface GameEntityRepository extends JpaRepository<GameEntity, UUID> {
}