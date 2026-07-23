package ru.sigris.service.dto;

import java.util.List;

public record PipelineResponse(
        String name,
        List<NodeResponse> nodes,
        List<EdgeResponse> edges
) {}