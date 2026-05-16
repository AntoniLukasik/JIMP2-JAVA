/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

/**
 *
 * @author Antoni Lukasik
 */
public class JIMP21 {

    public static void main(String[] args) {
        try {
            if (args.length == 2) {
                runFileLoader(args[0], args[1]);
            } else {
                printUsage();
                runSampleGraph();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java org.yourcompany.yourproject.JIMP21 <nodes.txt> <connections.txt>");
        System.out.println("If no arguments are supplied, a sample graph is created and saved to graph.json.");
        System.out.println();
    }

    private static void runFileLoader(String nodesPath, String connectionsPath) throws Exception {
        Graph g = new Graph();
        g.loadNodesFromTxt(new java.io.File(nodesPath));

        java.util.List<String> invalidLines = g.loadConnectionsFromTxt(new java.io.File(connectionsPath), new java.util.ArrayList<>());
        System.out.println("Loaded nodes: " + g.getNodes().size());
        System.out.println("Loaded connections: " + g.getConnections().size());
        if (!invalidLines.isEmpty()) {
            System.out.println("Invalid connection lines (skipped):");
            for (String line : invalidLines) {
                System.out.println("  " + line);
            }
        }
        System.out.println("Nodes:");
        for (Node n : g.allNodes()) {
            System.out.println("  " + n);
        }
        System.out.println("Connections:");
        for (Connection c : g.getConnections()) {
            System.out.println("  " + c);
        }
    }

    private static void runSampleGraph() throws Exception {
        Graph g = new Graph();
        g.addNode(new Node("A", 10, 20));
        g.addNode(new Node("B", 30, 40));
        g.addNode(new Node("C", 50, 60));

        g.addConnection(new Connection("A-B", "A", "B", 1.0));
        g.addConnection(new Connection("B-C", "B", "C", 1.0));

        java.io.File out = new java.io.File("graph.json");
        g.saveTo(out);
        System.out.println("Saved graph to: " + out.getAbsolutePath());

        Graph loaded = Graph.loadFrom(out);
        System.out.println("Loaded nodes:");
        for (Node n : loaded.allNodes()) {
            System.out.println("  " + n);
        }
        System.out.println("Loaded connections:");
        for (Connection c : loaded.getConnections()) {
            System.out.println("  " + c);
        }
    }
}
