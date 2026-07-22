package ru.sigris.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sigris.model.Edge;

import java.util.List;
import java.util.UUID;

@Repository
public interface EdgeRepository extends JpaRepository<Edge, UUID> {

    List<Edge> findAllByPipelineId(UUID pipelineId);

    boolean existsByFromIdAndToId(UUID fromId, UUID toId);
}