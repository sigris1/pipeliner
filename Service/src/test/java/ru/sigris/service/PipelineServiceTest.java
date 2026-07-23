package ru.sigris.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sigris.core.validator.Validator;
import ru.sigris.dao.EdgeRepository;
import ru.sigris.dao.NodeRepository;
import ru.sigris.dao.PipelineRepository;
import ru.sigris.model.Edge;
import ru.sigris.model.Node;
import ru.sigris.model.Pipeline;
import ru.sigris.service.dto.*;
import ru.sigris.service.exception.Exceptions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PipelineServiceTest {

    @Mock
    private PipelineRepository pipelineRepository;

    @Mock
    private NodeRepository nodeRepository;

    @Mock
    private EdgeRepository edgeRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private PipelineService pipelineService;

    @Test
    void createPipeline_Success() {
        String pName = "test-pipeline";
        PipelineRequest request = new PipelineRequest(
                pName,
                List.of("node1", "node2"),
                List.of(new EdgeRequest("node1", "node2"))
        );

        Pipeline savedPipeline = Pipeline.builder().name(pName).build();
        Node node1 = Node.builder().id(UUID.randomUUID()).name("node1").pipeline(savedPipeline).build();
        Node node2 = Node.builder().id(UUID.randomUUID()).name("node2").pipeline(savedPipeline).build();
        Edge savedEdge = Edge.builder().from(node1).to(node2).pipeline(savedPipeline).build();

        when(pipelineRepository.existsByName(pName)).thenReturn(false);
        when(pipelineRepository.save(any(Pipeline.class))).thenReturn(savedPipeline);
        when(nodeRepository.save(any(Node.class))).thenReturn(node1).thenReturn(node2);
        when(edgeRepository.save(any(Edge.class))).thenReturn(savedEdge);
        doNothing().when(validator).validateNoCycle(anyList(), anyList(), any(), any());
        when(validator.getTopologicalOrder(anyList(), anyList())).thenReturn(List.of("node1", "node2"));

        TraversalResponse response = pipelineService.createPipeline(request);

        assertNotNull(response);
        assertEquals(2, response.orderedNodeNames().size());
        verify(pipelineRepository, times(1)).save(any(Pipeline.class));
        verify(nodeRepository, times(2)).save(any(Node.class));
        verify(edgeRepository, times(1)).save(any(Edge.class));
        verify(validator, times(1)).validateNoCycle(anyList(), anyList(), any(), any());
        verify(validator, times(1)).getTopologicalOrder(anyList(), anyList());
    }

    @Test
    void createPipeline_ThrowsPipelineExist() {
        PipelineRequest request = new PipelineRequest("existing", List.of(), List.of());
        when(pipelineRepository.existsByName("existing")).thenReturn(true);

        PipelineExist exception = assertThrows(PipelineExist.class, () -> pipelineService.createPipeline(request));
        assertEquals("Pipeline already exist", exception.getMessage());
        verify(pipelineRepository, never()).save(any(Pipeline.class));
    }

    @Test
    void createPipeline_ThrowsNodeNotFound() {
        PipelineRequest request = new PipelineRequest(
                "new-pipe",
                List.of("node1"),
                List.of(new EdgeRequest("node1", "node2"))
        );

        when(pipelineRepository.existsByName("new-pipe")).thenReturn(false);
        when(pipelineRepository.save(any(Pipeline.class))).thenReturn(Pipeline.builder().name("new-pipe").build());
        when(nodeRepository.save(any(Node.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NodeNotFoundException exception = assertThrows(NodeNotFoundException.class, () -> pipelineService.createPipeline(request));
        assertEquals("Node not found", exception.getMessage());
    }

    @Test
    void addNode_Success() {
        Pipeline pipeline = Pipeline.builder().name("pipe1").build();
        Node newNode = Node.builder().name("newNode").pipeline(pipeline).build();

        when(pipelineRepository.findByName("pipe1")).thenReturn(Optional.of(pipeline));
        when(nodeRepository.existsByPipelineNameAndName("pipe1", "newNode")).thenReturn(false);
        when(nodeRepository.save(any(Node.class))).thenReturn(newNode);

        NodeResponse response = pipelineService.addNode("pipe1", "newNode");

        assertEquals("newNode", response.name());
        verify(nodeRepository, times(1)).save(any(Node.class));
    }

    @Test
    void addNode_ThrowsPipelineNotFound() {
        when(pipelineRepository.findByName("unknown")).thenReturn(Optional.empty());

        assertThrows(PipelineNotFoundException.class, () -> pipelineService.addNode("unknown", "node1"));
    }

    @Test
    void addNode_ThrowsNodeExistInPipeline() {
        when(pipelineRepository.findByName("pipe1")).thenReturn(Optional.of(Pipeline.builder().build()));
        when(nodeRepository.existsByPipelineNameAndName("pipe1", "existingNode")).thenReturn(true);

        assertThrows(NodeExistInPipeline.class, () -> pipelineService.addNode("pipe1", "existingNode"));
    }

    @Test
    void addEdge_Success() {
        String pName = "pipe1";
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Node from = Node.builder().id(id1).name("A").build();
        Node to = Node.builder().id(id2).name("B").build();

        when(pipelineRepository.findByName(pName)).thenReturn(Optional.of(Pipeline.builder().build()));
        when(nodeRepository.findByPipelineNameAndName(pName, "A")).thenReturn(Optional.of(from));
        when(nodeRepository.findByPipelineNameAndName(pName, "B")).thenReturn(Optional.of(to));
        when(edgeRepository.existsByFromIdAndToId(id1, id2)).thenReturn(false);
        when(nodeRepository.findAllByPipelineName(pName)).thenReturn(List.of(from, to));
        when(edgeRepository.findAllByPipelineName(pName)).thenReturn(List.of());
        when(edgeRepository.save(any(Edge.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(validator).validateNoCycle(anyList(), anyList(), eq(id1), eq(id2));

        pipelineService.addEdge(pName, "A", "B");

        verify(edgeRepository, times(1)).save(any(Edge.class));
        verify(validator, times(1)).validateNoCycle(anyList(), anyList(), eq(id1), eq(id2));
    }

    @Test
    void addEdge_ThrowsEdgeExist() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Node from = Node.builder().id(id1).build();
        Node to = Node.builder().id(id2).build();

        when(pipelineRepository.findByName("p1")).thenReturn(Optional.of(Pipeline.builder().build()));
        when(nodeRepository.findByPipelineNameAndName("p1", "A")).thenReturn(Optional.of(from));
        when(nodeRepository.findByPipelineNameAndName("p1", "B")).thenReturn(Optional.of(to));
        when(edgeRepository.existsByFromIdAndToId(id1, id2)).thenReturn(true);

        assertThrows(EdgeExist.class, () -> pipelineService.addEdge("p1", "A", "B"));
        verify(edgeRepository, never()).save(any(Edge.class));
    }

    @Test
    void addEdge_ThrowsPipelineHasntNode() {
        Node existingNode = Node.builder().id(UUID.randomUUID()).name("A").build();
        when(nodeRepository.findByPipelineNameAndName("p1", "A")).thenReturn(Optional.of(existingNode));

        when(pipelineRepository.findByName("p1")).thenReturn(Optional.of(Pipeline.builder().build()));
        when(nodeRepository.findByPipelineNameAndName("p1", "missingNode")).thenReturn(Optional.empty());

        assertThrows(PipelineHasntNode.class, () -> pipelineService.addEdge("p1", "A", "missingNode"));
    }

    @Test
    void getPipeline_Success() {
        Pipeline p = Pipeline.builder().name("p1").build();
        Node n = Node.builder().name("n1").build();

        when(pipelineRepository.findByName("p1")).thenReturn(Optional.of(p));
        when(nodeRepository.findAllByPipelineName("p1")).thenReturn(List.of(n));
        when(edgeRepository.findAllByPipelineName("p1")).thenReturn(List.of());

        PipelineResponse response = pipelineService.getPipeline("p1");

        assertEquals("p1", response.name());
        assertEquals(1, response.nodes().size());
    }

    @Test
    void getTraversal_Success() {
        when(pipelineRepository.findByName("p1")).thenReturn(Optional.of(Pipeline.builder().build()));
        when(nodeRepository.findAllByPipelineName("p1")).thenReturn(List.of());
        when(edgeRepository.findAllByPipelineName("p1")).thenReturn(List.of());
        when(validator.getTopologicalOrder(anyList(), anyList())).thenReturn(List.of("A", "B"));

        TraversalResponse response = pipelineService.getTraversal("p1");

        assertEquals(2, response.orderedNodeNames().size());
        verify(validator, times(1)).getTopologicalOrder(anyList(), anyList());
    }
}