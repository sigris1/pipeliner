package ru.sigris.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.sigris.service.PipelineService;
import ru.sigris.service.dto.*;

@RestController
@RequestMapping("/api/pipelines")
@RequiredArgsConstructor
@Tag(name = "Pipeline Management", description = "Pipeliner api")
public class PipelineController {

    private final PipelineService pipelineService;

    @PostMapping
    @Operation(summary = "Create pipeline", description = "Create Pipeline with initial nodes and edges")
    @ApiResponse(responseCode = "201", description = "Node successfully created")
    public ResponseEntity<TraversalResponse> createPipeline(@Valid @RequestBody PipelineRequest request) {
        TraversalResponse response = pipelineService.createPipeline(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{pipelineName}")
    @Operation(summary = "Get pipeline", description = "Get raw pipeline")
    public ResponseEntity<PipelineResponse> getPipeline(@PathVariable String pipelineName) {
        return ResponseEntity.ok(pipelineService.getPipeline(pipelineName));
    }

    @PostMapping("/{pipelineName}/nodes")
    @Operation(summary = "Create node", description = "Add node to existed pipeline")
    @ApiResponse(responseCode = "201", description = "Node successfully created")
    public ResponseEntity<NodeResponse> addNode(
            @PathVariable String pipelineName,
            @RequestParam String nodeName) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pipelineService.addNode(pipelineName, nodeName));
    }

    @GetMapping("/{pipelineName}/nodes/{nodeName}")
    @Operation(summary = "Get node", description = "Get information about node (now unused but in real project could be useful with node processing")
    public ResponseEntity<NodeResponse> getNode(
            @PathVariable String pipelineName,
            @PathVariable String nodeName) {
        return ResponseEntity.ok(pipelineService.getNodeByNames(pipelineName, nodeName));
    }

    @PostMapping("/{pipelineName}/dependencies")
    @Operation(summary = "Add edge", description = "Add edge between dependent nodes")
    @ApiResponse(responseCode = "201", description = "Node successfully created")
    public ResponseEntity<Void> addDependency(
            @PathVariable String pipelineName,
            @RequestParam String fromNode,
            @RequestParam String toNode) {
        pipelineService.addEdge(pipelineName, fromNode, toNode);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{pipelineName}/traversal")
    @Operation(summary = "Get traversal", description = "Get traversal of doing nodes")
    public ResponseEntity<TraversalResponse> getTopologicalOrder(@PathVariable String pipelineName) {
        return ResponseEntity.ok(pipelineService.getTraversal(pipelineName));
    }
}