package ru.sigris.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sigris.model.Pipeline;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, UUID> {

    Optional<Pipeline> findByName(String name);

    boolean existsByName(String name);
}