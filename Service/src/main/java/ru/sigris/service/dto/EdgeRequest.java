package ru.sigris.service.dto;

public record EdgeRequest(
        String fromNodeName,
        String toNodeName
) {}
