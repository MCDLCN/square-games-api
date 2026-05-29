/*
package com.mcdlcn.squaregamesapi.dao;

import com.mcdlcn.squaregamesapi.entity.GameEntity;
import com.mcdlcn.squaregamesapi.repository.GameEntityRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Primary
@Repository
public class JpaGameDao implements GameDao {

    private final GameEntityRepository repository;

    public JpaGameDao(GameEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Stream<GameEntity> findAll() {
        return repository.findAll().stream();
    }

    @Override
    public Optional<GameEntity> findById(UUID gameId) {
        return repository.findById(gameId);
    }

    @Override
    public GameEntity save(GameEntity gameEntity) {
        return repository.save(gameEntity);
    }

    @Override
    public void delete(UUID gameId) {
        repository.deleteById(gameId);
    }
}*/
