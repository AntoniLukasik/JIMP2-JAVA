package org.yourcompany.yourproject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Graph {
    private Map<String, Node> nodes = new LinkedHashMap<>();
    private List<Connection> connections = new ArrayList<>();

    public Graph() {
    }

    public Map<String, Node> getNodes() {
        return nodes;
    }

    public void setNodes(Map<String, Node> nodes) {
        this.nodes = nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public Collection<Node> allNodes() {
        return nodes.values();
    }

    public boolean updateNodeCoordinates(String id, double x, double y) {
        Node n = nodes.get(id);
        if (n == null) return false;
        n.setX(x);
        n.setY(y);
        return true;
    }

    public boolean renameNode(String oldId, String newId) {
        if (!nodes.containsKey(oldId)) return false;
        if (nodes.containsKey(newId)) return false;
        Node n = nodes.remove(oldId);
        n.setId(newId);
        nodes.put(newId, n);
        for (Connection c : connections) {
            if (oldId.equals(c.getFrom())) c.setFrom(newId);
            if (oldId.equals(c.getTo())) c.setTo(newId);
        }
        return true;
    }

    public boolean removeNode(String id) {
        Node removed = nodes.remove(id);
        if (removed == null) return false;
        connections.removeIf(c -> id.equals(c.getFrom()) || id.equals(c.getTo()));
        return true;
    }

    public void addConnection(Connection c) {
        connections.add(c);
    }

    public void addConnection(String from, String to) {
        addConnection(new Connection(from, to));
    }

    public void saveTo(File file) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        om.writeValue(file, this);
    }

    public static Graph loadFrom(File file) throws IOException {
        ObjectMapper om = new ObjectMapper();
        return om.readValue(file, Graph.class);
    }

    private static List<String> parseQuotedFields(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;
        Matcher m = Pattern.compile("'([^']*)'|\"([^\"]*)\"|(\\S+)").matcher(line);
        while (m.find()) {
            String v = m.group(1);
            if (v == null) v = m.group(2);
            if (v == null) v = m.group(3);
            out.add(v);
        }
        return out;
    }

    public void loadNodesFromTxt(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            List<String> f = parseQuotedFields(trimmed);
            if (f.size() >= 3) {
                String id = f.get(0);
                try {
                    double x = Double.parseDouble(f.get(1));
                    double y = Double.parseDouble(f.get(2));
                    Node n = nodes.get(id);
                    if (n == null) {
                        addNode(new Node(id, x, y));
                    } else {
                        n.setX(x);
                        n.setY(y);
                    }
                } catch (NumberFormatException ex) {
                    // skip malformed line
                }
            }
        }
    }

    public void saveNodesToTxt(File file) throws IOException {
        List<String> out = nodes.values().stream()
                .map(n -> String.format("'%s' '%s' '%s'", n.getId(), n.getX(), n.getY()))
                .collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardCharsets.UTF_8);
    }

    public void loadConnectionsFromTxt(File file) throws IOException {
        loadConnectionsFromTxt(file, new ArrayList<>());
    }

    public List<String> loadConnectionsFromTxt(File file, List<String> invalidLines) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
            List<String> f = parseQuotedFields(trimmed);
            if (f.size() >= 4) {
                String name = f.get(0);
                String nodeA = f.get(1);
                String nodeB = f.get(2);
                try {
                    double weight = Double.parseDouble(f.get(3));
                    if (nodes.containsKey(nodeA) && nodes.containsKey(nodeB)) {
                        connections.add(new Connection(name, nodeA, nodeB, weight));
                    } else {
                        invalidLines.add(trimmed);
                    }
                } catch (NumberFormatException ex) {
                    invalidLines.add(trimmed);
                }
            } else {
                invalidLines.add(trimmed);
            }
        }
        return invalidLines;
    }

    public void saveConnectionsToTxt(File file) throws IOException {
        List<String> out = connections.stream()
                .map(c -> String.format("'%s' '%s' '%s' '%s'", c.getName(), c.getFrom(), c.getTo(), c.getWeight()))
                .collect(Collectors.toList());
        Files.write(file.toPath(), out, StandardCharsets.UTF_8);
    }
}
