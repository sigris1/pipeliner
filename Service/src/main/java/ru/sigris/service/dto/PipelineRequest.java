package ru.sigris.service.dto;

import java.util.List;

public record PipelineRequest(
        String pipelineName,
        List<String> nodes,
        List<EdgeRequest> edges
) {}
