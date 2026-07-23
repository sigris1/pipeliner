package ru.sigris.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.sigris.core.exception.Exceptions;
import ru.sigris.core.validator.Validator;
import ru.sigris.model.Edge;
import ru.sigris.model.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PipelinerCoreTest {

    private Validator validator;
    private List<Node> nodes;
    private List<Edge> edges;
    private UUID idA, idB, idC, idD, idE;

    @BeforeEach
    void setUp() {
        validator = new Validator();
        nodes = new ArrayList<>();
        edges = new ArrayList<>();

        idA = UUID.randomUUID();
        idB = UUID.randomUUID();
        idC = UUID.randomUUID();
        idD = UUID.randomUUID();
        idE = UUID.randomUUID();

        nodes.add(Node.builder().id(idA).name("NodeA").build());
        nodes.add(Node.builder().id(idB).name("NodeB").build());
        nodes.add(Node.builder().id(idC).name("NodeC").build());
        nodes.add(Node.builder().id(idD).name("NodeD").build());
        nodes.add(Node.builder().id(idE).name("NodeE").build());
    }

    @Test
    void validateNoCycle_ShouldPass_WhenAddingValidEdge() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());

        assertDoesNotThrow(() ->
                validator.validateNoCycle(nodes, edges, idB, idC)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenCreatingCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idC, idA)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenSelfLoop() {
        assertThrows(Exceptions.SelfCyclingException.class, () ->
                validator.validateNoCycle(nodes, edges, idA, idA)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenCycleThroughThreeNodes() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idC, idA)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenCycleThroughFourNodes() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idD, idA)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenIndirectCycleCreated() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idD, idB)
        );
    }

    @Test
    void validateNoCycle_ShouldPass_WhenDiamondShapeWithoutCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(3)).build());

        assertDoesNotThrow(() ->
                validator.validateNoCycle(nodes, edges, idC, idD)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenDiamondShapeWithCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idD, idA)
        );
    }

    @Test
    void validateNoCycle_ShouldPass_WhenEmptyEdges() {
        assertDoesNotThrow(() ->
                validator.validateNoCycle(nodes, edges, idA, idB)
        );
    }

    @Test
    void validateNoCycle_ShouldPass_WhenLinearChain() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(3)).to(nodes.get(4)).build());

        assertDoesNotThrow(() ->
                validator.validateNoCycle(nodes, edges, idA, idE)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenCycleInMiddleOfGraph() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(3)).to(nodes.get(1)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idA, idE)
        );
    }

    @Test
    void validateNoCycle_ShouldThrow_WhenMultipleCyclesExist() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(0)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(3)).to(nodes.get(2)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.validateNoCycle(nodes, edges, idA, idE)
        );
    }

    @Test
    void validateNoCycle_ShouldPass_WhenParallelPaths() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(4)).build());

        assertDoesNotThrow(() ->
                validator.validateNoCycle(nodes, edges, idB, idC)
        );
    }

    @Test
    void getTopologicalOrder_ShouldReturnCorrectOrder() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());

        List<String> order = validator.getTopologicalOrder(nodes, edges);

        assertEquals(5, order.size());
        assertTrue(order.indexOf("NodeA") < order.indexOf("NodeB"));
        assertTrue(order.indexOf("NodeB") < order.indexOf("NodeC"));
    }

    @Test
    void getTopologicalOrder_ShouldThrow_WhenGraphHasCycle() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(0)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.getTopologicalOrder(nodes, edges)
        );
    }

    @Test
    void getTopologicalOrder_ShouldWork_WithEmptyEdges() {
        List<String> order = validator.getTopologicalOrder(nodes, edges);

        assertEquals(5, order.size());
    }

    @Test
    void getTopologicalOrder_ShouldWork_WithDiamondShape() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());

        List<String> order = validator.getTopologicalOrder(nodes, edges);

        assertEquals(5, order.size());
        assertTrue(order.indexOf("NodeA") < order.indexOf("NodeB"));
        assertTrue(order.indexOf("NodeA") < order.indexOf("NodeC"));
        assertTrue(order.indexOf("NodeB") < order.indexOf("NodeD"));
        assertTrue(order.indexOf("NodeC") < order.indexOf("NodeD"));
    }

    @Test
    void getTopologicalOrder_ShouldWork_WithMultipleStartNodes() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());

        List<String> order = validator.getTopologicalOrder(nodes, edges);

        assertEquals(5, order.size());
        assertTrue(order.indexOf("NodeA") < order.indexOf("NodeC"));
        assertTrue(order.indexOf("NodeB") < order.indexOf("NodeC"));
        assertTrue(order.indexOf("NodeC") < order.indexOf("NodeD"));
    }

    @Test
    void getTopologicalOrder_ShouldWork_WithLongChain() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(1)).build());
        edges.add(Edge.builder().from(nodes.get(1)).to(nodes.get(2)).build());
        edges.add(Edge.builder().from(nodes.get(2)).to(nodes.get(3)).build());
        edges.add(Edge.builder().from(nodes.get(3)).to(nodes.get(4)).build());

        List<String> order = validator.getTopologicalOrder(nodes, edges);

        assertEquals(5, order.size());
        assertEquals("NodeA", order.get(0));
        assertEquals("NodeB", order.get(1));
        assertEquals("NodeC", order.get(2));
        assertEquals("NodeD", order.get(3));
        assertEquals("NodeE", order.get(4));
    }

    @Test
    void getTopologicalOrder_ShouldThrow_WhenSelfLoop() {
        edges.add(Edge.builder().from(nodes.get(0)).to(nodes.get(0)).build());

        assertThrows(Exceptions.CycleDetectedException.class, () ->
                validator.getTopologicalOrder(nodes, edges)
        );
    }
}