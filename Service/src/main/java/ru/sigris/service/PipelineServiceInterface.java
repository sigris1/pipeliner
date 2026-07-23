package ru.sigris.service;

import ru.sigris.service.dto.*;

public interface PipelineServiceInterface {

    TraversalResponse createPipeline(PipelineRequest request);

    NodeResponse addNode(String pipelineName, String nodeName);

    void addEdge(String pipelineName, String fromNodeName, String toNodeName);

    PipelineResponse getPipeline(String pipelineName);

    TraversalResponse getTraversal(String pipelineName);

    NodeResponse getNodeByNames(String pipelineName, String nodeName);
}
