package ru.sigris.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.sigris.model.Node;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NodeRepository extends JpaRepository<Node, UUID> {
    List<Node> findAllByPipelineId(UUID pipelineId);

    boolean existsByPipelineIdAndName(UUID pipelineId, String name);

    Optional<Node> findByPipelineNameAndName(String pipelineName, String nodeName);

    boolean existsByPipelineNameAndName(String pipelineName, String nodeName);

    List<Node> findAllByPipelineName(String pipelineName);
}