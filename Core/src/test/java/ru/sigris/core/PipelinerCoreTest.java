package ru.sigris.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sigris.core.exception.Exceptions;
import ru.sigris.model.Edge;
import ru.sigris.model.Node;

import ru.sigris.core.validator.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PipelinerCoreTest {

    private List<Node> nodes;
    private List<Edge> edges;
    private UUID idA, idB, idC;

    @BeforeEach
    void setUp() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        idA = UUID.randomUUID();
        idB = UUID.randomUUID();
        idC = UUID.randomUUID();

        nodes.add(Node.builder().id(idA).name("NodeA").build());
        nodes.add(Node.builder().id(idB).name("NodeB").build());
        nodes.add(Node.builder().id(idC).name("NodeC").build());
    }

    @Test
    void validateNoCycle_ShouldPass_WhenAddingValidEdge() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());

        assertDoesNotThrow(() ->
                Validator.validateNoCycle(nodes, edges, idB, idC)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenCreatingCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                Validator.validateNoCycle(nodes, edges, idC, idA)
        );
    }

    @Test
    void getTopologicalOrder_ShouldReturnCorrectOrder() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());

        List<String> order = Validator.getTopologicalOrder(nodes, edges);

        assertEquals(3, order.size());
        assertEquals("NodeA", order.get(0));
        assertEquals("NodeB", order.get(1));
        assertEquals("NodeC", order.get(2));
    }

    @Test
    void getTopologicalOrder_ShouldThrow_WhenGraphHasCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(0)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                Validator.getTopologicalOrder(nodes, edges)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenSelfLoop() {
        assertThrows(Exceptions.SelfCyclingException.class, () ->
                Validator.validateNoCycle(nodes, edges, idA, idA)
        );
    }
}