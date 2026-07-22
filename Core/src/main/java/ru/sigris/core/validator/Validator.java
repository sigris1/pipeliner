package ru.sigris.core.validator;

import ru.sigris.core.exception.Exceptions.CycleDetectedException;
import ru.sigris.model.Edge;
import ru.sigris.model.Node;

import java.util.*;

public class Validator {

    public static void validateNoCycle(List<Node> nodes, List<Edge> edges, UUID newFromId, UUID newToId) {
        Map<UUID, List<UUID>> adjList = buildAdjacencyList(nodes, edges, newFromId, newToId);

        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();

        for (Node node : nodes) {
            if (!visited.contains(node.getId())) {
                if (hasCycleDFS(node.getId(), adjList, visited, recursionStack)) {
                    throw new CycleDetectedException();
                }
            }
        }
    }

    private static boolean hasCycleDFS(UUID current, Map<UUID, List<UUID>> adjList,
                                       Set<UUID> visited, Set<UUID> recursionStack) {
        visited.add(current);
        recursionStack.add(current);

        for (UUID neighbor : adjList.getOrDefault(current, Collections.emptyList())) {
            if (!visited.contains(neighbor)) {
                if (hasCycleDFS(neighbor, adjList, visited, recursionStack)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                return true;
            }
        }

        recursionStack.remove(current);
        return false;
    }

    public static List<String> getTopologicalOrder(List<Node> nodes, List<Edge> edges) {
        Map<UUID, String> nodeNameMap = new HashMap<>();
        Map<UUID, Integer> inDegree = new HashMap<>();
        Map<UUID, List<UUID>> adjList = new HashMap<>();

        for (Node node : nodes) {
            nodeNameMap.put(node.getId(), node.getName());
            inDegree.put(node.getId(), 0);
            adjList.put(node.getId(), new ArrayList<>());
        }

        for (Edge edge : edges) {
            adjList.get(edge.getFrom().getId()).add(edge.getTo().getId());
            inDegree.merge(edge.getTo().getId(), 1, Integer::sum);
        }

        Queue<UUID> queue = new LinkedList<>();
        for (Map.Entry<UUID, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            result.add(nodeNameMap.get(current));

            for (UUID neighbor : adjList.getOrDefault(current, Collections.emptyList())) {
                int newDegree = inDegree.get(neighbor) - 1;
                inDegree.put(neighbor, newDegree);

                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (result.size() != nodes.size()) {
            throw new CycleDetectedException();
        }

        return result;
    }

    private static Map<UUID, List<UUID>> buildAdjacencyList(List<Node> nodes, List<Edge> edges,
                                                            UUID newFromId, UUID newToId) {
        Map<UUID, List<UUID>> adjList = new HashMap<>();

        for (Node node : nodes) {
            adjList.put(node.getId(), new ArrayList<>());
        }

        for (Edge edge : edges) {
            adjList.get(edge.getFrom().getId()).add(edge.getTo().getId());
        }

        if (newFromId != null && newToId != null) {
            adjList.get(newFromId).add(newToId);
        }

        return adjList;
    }
}
