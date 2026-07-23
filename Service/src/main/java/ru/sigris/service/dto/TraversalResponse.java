package ru.sigris.service.dto;

import java.util.List;

public record TraversalResponse(
        List<String> orderedNodeNames
) {}