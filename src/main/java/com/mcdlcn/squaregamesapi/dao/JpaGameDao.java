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
public class JpaGameDao {

    private final GameEntityRepository repository;

    public JpaGameDao(GameEntityRepository repository) {
        this.repository = repository;
    }

    public Stream<GameEntity> findAll() {
        return repository.findAll().stream();
    }

    public Optional<GameEntity> findById(UUID gameId) {
        return repository.findById(gameId);
    }

    public GameEntity save(GameEntity gameEntity) {
        return repository.save(gameEntity);
    }

    public void delete(UUID gameId) {
        repository.deleteById(gameId);
    }
}