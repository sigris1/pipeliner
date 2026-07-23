package ru.sigris.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sigris.core.validator.Validator;
import ru.sigris.service.exception.Exceptions.PipelineNotFoundException;
import ru.sigris.service.exception.Exceptions.PipelineExist;
import ru.sigris.service.exception.Exceptions.NodeNotFoundException;
import ru.sigris.service.exception.Exceptions.NodeExistInPipeline;
import ru.sigris.service.exception.Exceptions.PipelineHasntNode;
import ru.sigris.service.exception.Exceptions.EdgeExist;
import ru.sigris.dao.EdgeRepository;
import ru.sigris.dao.NodeRepository;
import ru.sigris.dao.PipelineRepository;
import ru.sigris.model.Edge;
import ru.sigris.model.Node;
import ru.sigris.model.Pipeline;
import ru.sigris.service.dto.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PipelineService implements PipelineServiceInterface {

    private final PipelineRepository pipelineRepository;
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final Validator validator;

    @Override
    public TraversalResponse createPipeline(PipelineRequest request) {
        if (pipelineRepository.existsByName(request.pipelineName())) {
            throw new PipelineExist();
        }

        Pipeline pipeline = pipelineRepository.save(
                Pipeline.builder().name(request.pipelineName()).build()
        );

        List<String> nodeNames = request.nodes() == null ? List.of() : request.nodes();
        List<EdgeRequest> deps = request.edges() == null ? List.of() : request.edges();

        Map<String, Node> nodeMap = nodeNames.stream()
                .map(nodeName -> Node.builder().name(nodeName).pipeline(pipeline).build())
                .map(nodeRepository::save)
                .collect(Collectors.toMap(Node::getName, n -> n));

        List<Node> allNodes = new ArrayList<>(nodeMap.values());
        List<Edge> edges = new ArrayList<>();

        for (EdgeRequest dep : deps) {
            Node from = nodeMap.get(dep.fromNodeName());
            Node to = nodeMap.get(dep.toNodeName());

            if (from == null || to == null) {
                throw new NodeNotFoundException();
            }

            validator.validateNoCycle(allNodes, edges, from.getId(), to.getId());

            Edge edge = edgeRepository.save(
                    Edge.builder().from(from).to(to).pipeline(pipeline).build()
            );
            edges.add(edge);
        }

        return new TraversalResponse(validator.getTopologicalOrder(allNodes, edges));
    }

    @Override
    @Transactional
    public NodeResponse addNode(String pipelineName, String nodeName) {
        Pipeline pipeline = pipelineRepository.findByName(pipelineName)
                .orElseThrow(PipelineNotFoundException::new);

        if (nodeRepository.existsByPipelineNameAndName(pipelineName, nodeName)) {
            throw new NodeExistInPipeline();
        }

        Node node = Node.builder()
                .name(nodeName)
                .pipeline(pipeline)
                .build();

        Node savedNode = nodeRepository.save(node);
        return new NodeResponse(savedNode.getName());
    }

    @Override
    @Transactional
    public void addEdge(String pipelineName, String fromNodeName, String toNodeName) {
        Pipeline pipeline = pipelineRepository.findByName(pipelineName)
                .orElseThrow(PipelineNotFoundException::new);

        Node fromNode = nodeRepository.findByPipelineNameAndName(pipelineName, fromNodeName)
                .orElseThrow(PipelineHasntNode::new);
        Node toNode = nodeRepository.findByPipelineNameAndName(pipelineName, toNodeName)
                .orElseThrow(PipelineHasntNode::new);


        if (edgeRepository.existsByFromIdAndToId(fromNode.getId(), toNode.getId())) {
            throw new EdgeExist();
        }

        List<Node> allNodes = nodeRepository.findAllByPipelineName(pipelineName);
        List<Edge> allEdges = edgeRepository.findAllByPipelineName(pipelineName);

        validator.validateNoCycle(allNodes, allEdges, fromNode.getId(), toNode.getId());

        Edge edge = Edge.builder()
                .from(fromNode)
                .to(toNode)
                .pipeline(pipeline)
                .build();

        edgeRepository.save(edge);
    }

    @Override
    @Transactional(readOnly = true)
    public PipelineResponse getPipeline(String pipelineName) {
        Pipeline pipeline = pipelineRepository.findByName(pipelineName)
                .orElseThrow(PipelineNotFoundException::new);

        List<Node> nodes = nodeRepository.findAllByPipelineName(pipelineName);
        List<Edge> edges = edgeRepository.findAllByPipelineName(pipelineName);

        return new PipelineResponse(
                pipeline.getName(),
                nodes.stream().map(n -> new NodeResponse(n.getName())).toList(),
                edges.stream().map(e -> new EdgeResponse(e.getFrom().getName(), e.getTo().getName())).toList()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TraversalResponse getTraversal(String pipelineName) {
        pipelineRepository.findByName(pipelineName)
                .orElseThrow(PipelineNotFoundException::new);

        List<Node> nodes = nodeRepository.findAllByPipelineName(pipelineName);
        List<Edge> edges = edgeRepository.findAllByPipelineName(pipelineName);

        List<String> order = validator.getTopologicalOrder(nodes, edges);
        return new TraversalResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public NodeResponse getNodeByNames(String pipelineName, String nodeName) {
        Node node = nodeRepository.findByPipelineNameAndName(pipelineName, nodeName)
                .orElseThrow(PipelineHasntNode::new);

        return new NodeResponse(node.getName());
    }
}
