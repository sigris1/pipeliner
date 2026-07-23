package ru.sigris.service.dto;

public record EdgeResponse(
        String fromNodeName,
        String toNodeName
) {}