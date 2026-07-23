package ru.sigris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.sigris.controller.PipelineExceptionHandler;
import ru.sigris.service.PipelineService;
import ru.sigris.service.dto.*;
import ru.sigris.service.exception.Exceptions.*;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PipelineControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PipelineService pipelineService;

    @InjectMocks
    private PipelineController pipelineController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pipelineController)
                .setControllerAdvice(new PipelineExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }


    @Test
    void createPipeline_Returns201() throws Exception {
        TraversalResponse response = new TraversalResponse(List.of("node1", "node2"));
        when(pipelineService.createPipeline(any(PipelineRequest.class))).thenReturn(response);

        PipelineRequest request = new PipelineRequest(
                "test-pipeline",
                List.of("node1", "node2"),
                List.of(new EdgeRequest("node1", "node2"))
        );

        mockMvc.perform(post("/api/pipelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderedNodeNames").isArray())
                .andExpect(jsonPath("$.orderedNodeNames[0]").value("node1"))
                .andExpect(jsonPath("$.orderedNodeNames[1]").value("node2"));
    }

    @Test
    void createPipeline_Returns409_WhenPipelineExists() throws Exception {
        when(pipelineService.createPipeline(any(PipelineRequest.class)))
                .thenThrow(new PipelineExist());

        PipelineRequest request = new PipelineRequest("existing", List.of(), List.of());

        mockMvc.perform(post("/api/pipelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Pipeline already exist"))
                .andExpect(jsonPath("$.path").value("/api/pipelines"));
    }

    @Test
    void createPipeline_Returns404_WhenNodeNotFound() throws Exception {
        when(pipelineService.createPipeline(any(PipelineRequest.class)))
                .thenThrow(new NodeNotFoundException());

        PipelineRequest request = new PipelineRequest("test", List.of("A"), List.of(new EdgeRequest("A", "B")));

        mockMvc.perform(post("/api/pipelines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Node not found"));
    }

    @Test
    void getPipeline_Returns200() throws Exception {
        PipelineResponse response = new PipelineResponse(
                "test-pipe",
                List.of(new NodeResponse("A"), new NodeResponse("B")),
                List.of(new EdgeResponse("A", "B"))
        );
        when(pipelineService.getPipeline("test-pipe")).thenReturn(response);

        mockMvc.perform(get("/api/pipelines/test-pipe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test-pipe"))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.nodes[0].name").value("A"))
                .andExpect(jsonPath("$.edges[0].fromNodeName").value("A"))
                .andExpect(jsonPath("$.edges[0].toNodeName").value("B"));
    }

    @Test
    void getPipeline_Returns404_WhenNotFound() throws Exception {
        when(pipelineService.getPipeline("unknown"))
                .thenThrow(new PipelineNotFoundException());

        mockMvc.perform(get("/api/pipelines/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pipeline not found"))
                .andExpect(jsonPath("$.path").value("/api/pipelines/unknown"));
    }

    @Test
    void addNode_Returns201() throws Exception {
        when(pipelineService.addNode(anyString(), anyString()))
                .thenReturn(new NodeResponse("newNode"));

        mockMvc.perform(post("/api/pipelines/pipe1/nodes")
                        .param("nodeName", "newNode"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("newNode"));
    }

    @Test
    void addNode_Returns404_WhenPipelineNotFound() throws Exception {
        when(pipelineService.addNode(anyString(), anyString()))
                .thenThrow(new PipelineNotFoundException());

        mockMvc.perform(post("/api/pipelines/unknown/nodes")
                        .param("nodeName", "node1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pipeline not found"));
    }

    @Test
    void addNode_Returns409_WhenNodeExists() throws Exception {
        when(pipelineService.addNode(anyString(), anyString()))
                .thenThrow(new NodeExistInPipeline());

        mockMvc.perform(post("/api/pipelines/pipe1/nodes")
                        .param("nodeName", "existing"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Node already exist in pipeline"));
    }

    @Test
    void getNode_Returns200() throws Exception {
        when(pipelineService.getNodeByNames(anyString(), anyString()))
                .thenReturn(new NodeResponse("nodeA"));

        mockMvc.perform(get("/api/pipelines/pipe1/nodes/nodeA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("nodeA"));
    }

    @Test
    void getNode_Returns400_WhenNodeNotInPipeline() throws Exception {
        when(pipelineService.getNodeByNames(anyString(), anyString()))
                .thenThrow(new PipelineHasntNode());

        mockMvc.perform(get("/api/pipelines/pipe1/nodes/missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pipeline has not node"));
    }

    @Test
    void addDependency_Returns201() throws Exception {
        doNothing().when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/pipe1/dependencies")
                        .param("fromNode", "A")
                        .param("toNode", "B"))
                .andExpect(status().isCreated());

        verify(pipelineService, times(1)).addEdge("pipe1", "A", "B");
    }

    @Test
    void addDependency_Returns404_WhenPipelineNotFound() throws Exception {
        doThrow(new PipelineNotFoundException())
                .when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/unknown/dependencies")
                        .param("fromNode", "A")
                        .param("toNode", "B"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pipeline not found"));
    }

    @Test
    void addDependency_Returns400_WhenNodeNotInPipeline() throws Exception {
        doThrow(new PipelineHasntNode())
                .when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/pipe1/dependencies")
                        .param("fromNode", "A")
                        .param("toNode", "missing"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Pipeline has not node"));
    }

    @Test
    void addDependency_Returns409_WhenEdgeExists() throws Exception {
        doThrow(new EdgeExist())
                .when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/pipe1/dependencies")
                        .param("fromNode", "A")
                        .param("toNode", "B"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Edge already exist"));
    }

    @Test
    void addDependency_Returns400_WhenCycleDetected() throws Exception {
        doThrow(new ru.sigris.core.exception.Exceptions.CycleDetectedException())
                .when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/pipe1/dependencies")
                        .param("fromNode", "B")
                        .param("toNode", "A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cycle detected"));
    }

    @Test
    void addDependency_Returns400_WhenSelfCycling() throws Exception {
        doThrow(new ru.sigris.core.exception.Exceptions.SelfCyclingException())
                .when(pipelineService).addEdge(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/pipelines/pipe1/dependencies")
                        .param("fromNode", "A")
                        .param("toNode", "A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Self cycling"));
    }

    @Test
    void getTraversal_Returns200() throws Exception {
        when(pipelineService.getTraversal(anyString()))
                .thenReturn(new TraversalResponse(List.of("A", "B", "C")));

        mockMvc.perform(get("/api/pipelines/pipe1/traversal"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderedNodeNames").isArray())
                .andExpect(jsonPath("$.orderedNodeNames[0]").value("A"))
                .andExpect(jsonPath("$.orderedNodeNames[1]").value("B"))
                .andExpect(jsonPath("$.orderedNodeNames[2]").value("C"));
    }

    @Test
    void getTraversal_Returns404_WhenPipelineNotFound() throws Exception {
        when(pipelineService.getTraversal(anyString()))
                .thenThrow(new PipelineNotFoundException());

        mockMvc.perform(get("/api/pipelines/unknown/traversal"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Pipeline not found"));
    }
}