/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package org.yourcompany.yourproject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Antoni Lukasik
 */
public class JIMP21 {

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                printUsage();
                runSampleGraph();
                return;
            }
            if (hasFlag(args, "-h")) {
                printUsage();
                return;
            }
            if (args.length == 2 && !args[0].startsWith("-")) {
                runFileLoader(args[0], args[1]);
                return;
            }
            CliOptions options = parseArgs(args);
            if (options == null) {
                printUsage();
                return;
            }
            runLayout(options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (arg.equals(flag)) return true;
        }
        return false;
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java org.yourcompany.yourproject.JIMP21 -i <input> -o <output> -a <fr|cp> [-iter <n>] [-f <txt|bin>] [-svg] [-h]");
        System.out.println("Flags:");
        System.out.println("  -i <ścieżka>    Required input file containing edges (name nodeA nodeB weight)");
        System.out.println("  -o <ścieżka>    Optional output file for node coordinates. Default: output.txt");
        System.out.println("  -a <nazwa>      Required algorithm: fr or cp");
        System.out.println("  -iter <liczba>  Optional iterations for Fruchterman-Reingold (default 100)");
        System.out.println("  -f <typ>        Optional output format: txt or bin (default txt)");
        System.out.println("  -svg            Optional generate SVG visualization with coordinates");
        System.out.println("  -h              Optional show this help message");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java org.yourcompany.yourproject.JIMP21 -i graf_testowy.txt -o dane_java.bin -a fr -iter 500 -f bin -svg");
        System.out.println();
        System.out.println("Legacy mode: java org.yourcompany.yourproject.JIMP21 <nodes.txt> <connections.txt>");
    }

    private static CliOptions parseArgs(String[] args) {
        CliOptions options = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-i":
                    if (i + 1 >= args.length) return null;
                    options.inputFile = args[++i];
                    break;
                case "-o":
                    if (i + 1 >= args.length) return null;
                    options.outputFile = args[++i];
                    break;
                case "-a":
                    if (i + 1 >= args.length) return null;
                    options.algorithm = args[++i];
                    break;
                case "-iter":
                    if (i + 1 >= args.length) return null;
                    try {
                        options.iterations = Integer.parseInt(args[++i]);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                    break;
                case "-f":
                    if (i + 1 >= args.length) return null;
                    options.format = args[++i];
                    break;
                case "-svg":
                    options.svg = true;
                    break;
                case "-h":
                    options.help = true;
                    break;
                default:
                    return null;
            }
        }
        if (options.inputFile == null || options.algorithm == null) {
            return null;
        }
        if (options.outputFile == null) {
            options.outputFile = "output.txt";
        }
        if (options.iterations <= 0) {
            options.iterations = 100;
        }
        if (options.format == null) {
            options.format = "txt";
        }
        if (!options.format.equals("txt") && !options.format.equals("bin")) {
            return null;
        }
        if (!options.algorithm.equals("fr") && !options.algorithm.equals("cp")) {
            return null;
        }
        return options;
    }

    private static void runLayout(CliOptions options) throws Exception {
        Graph g = new Graph();
        List<String> invalidLines = new ArrayList<>();
        g.loadConnectionsFromTxt(new File(options.inputFile), invalidLines);

        if (!invalidLines.isEmpty()) {
            System.out.println("Invalid connection lines (skipped):");
            for (String line : invalidLines) {
                System.out.println("  " + line);
            }
        }

        if (options.algorithm.equals("fr")) {
            applyFruchtermanReingold(g, options.iterations, 800, 800);
        } else {
            applyChrobakPayne(g, 800, 800);
        }

        saveNodeCoordinates(g, new File(options.outputFile), options.format);
        if (options.svg) {
            File svgFile = new File(getBaseName(options.outputFile) + ".svg");
            saveSvg(g, svgFile, 800, 800);
            System.out.println("Saved SVG to: " + svgFile.getAbsolutePath());
        }

        System.out.println("Saved coordinates to: " + new File(options.outputFile).getAbsolutePath());
        System.out.println("Nodes:");
        for (Node n : g.allNodes()) {
            System.out.println("  " + n);
        }
        System.out.println("Connections:");
        for (Connection c : g.getConnections()) {
            System.out.println("  " + c);
        }
    }

    private static void saveNodeCoordinates(Graph g, File file, String format) throws IOException {
        if (format.equals("txt")) {
            List<String> out = new ArrayList<>();
            for (Node node : g.allNodes()) {
                out.add(String.format("%s %s %s", node.getId(), node.getX(), node.getY()));
            }
            Files.write(file.toPath(), out, StandardCharsets.UTF_8);
        } else {
            try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
                dos.writeInt(g.getNodes().size());
                for (Node node : g.allNodes()) {
                    dos.writeUTF(node.getId());
                    dos.writeDouble(node.getX());
                    dos.writeDouble(node.getY());
                }
            }
        }
    }

    private static String getBaseName(String path) {
        int dot = path.lastIndexOf('.');
        if (dot <= 0) {
            return path;
        }
        return path.substring(0, dot);
    }

    private static void saveSvg(Graph g, File svgFile, int width, int height) throws IOException {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        for (Node node : g.allNodes()) {
            minX = Math.min(minX, node.getX());
            minY = Math.min(minY, node.getY());
            maxX = Math.max(maxX, node.getX());
            maxY = Math.max(maxY, node.getY());
        }
        if (minX == Double.POSITIVE_INFINITY) {
            minX = minY = 0;
            maxX = maxY = 1;
        }
        double pad = 40;
        double scaleX = (width - 2 * pad) / Math.max(1.0, maxX - minX);
        double scaleY = (height - 2 * pad) / Math.max(1.0, maxY - minY);
        double scale = Math.min(scaleX, scaleY);

        List<String> lines = new ArrayList<>();
        lines.add("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\">\n");
        for (Connection c : g.getConnections()) {
            Node a = g.getNode(c.getFrom());
            Node b = g.getNode(c.getTo());
            if (a == null || b == null) continue;
            double ax = pad + (a.getX() - minX) * scale;
            double ay = pad + (a.getY() - minY) * scale;
            double bx = pad + (b.getX() - minX) * scale;
            double by = pad + (b.getY() - minY) * scale;
            lines.add(String.format("<line x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" stroke=\"black\" stroke-width=1 />", ax, ay, bx, by));
        }
        for (Node node : g.allNodes()) {
            double x = pad + (node.getX() - minX) * scale;
            double y = pad + (node.getY() - minY) * scale;
            lines.add(String.format("<circle cx=\"%s\" cy=\"%s\" r=\"8\" fill=\"red\" />", x, y));
            lines.add(String.format("<text x=\"%s\" y=\"%s\" font-size=12 text-anchor=\"middle\" dy=\"-10\">%s</text>", x, y, node.getId()));
        }
        lines.add("</svg>");
        Files.write(svgFile.toPath(), lines, StandardCharsets.UTF_8);
    }

    private static void applyFruchtermanReingold(Graph g, int iterations, int width, int height) {
        int n = g.getNodes().size();
        if (n == 0) return;
        double area = width * height;
        double k = Math.sqrt(area / n);
        Random random = new Random(1);
        for (Node node : g.allNodes()) {
            node.setX(random.nextDouble() * width);
            node.setY(random.nextDouble() * height);
        }
        double temperature = width / 10.0;
        for (int iter = 0; iter < iterations; iter++) {
            Map<String, double[]> disp = new HashMap<>();
            for (Node v : g.allNodes()) {
                disp.put(v.getId(), new double[] {0.0, 0.0});
            }
            for (Node v : g.allNodes()) {
                for (Node u : g.allNodes()) {
                    if (v == u) continue;
                    double dx = v.getX() - u.getX();
                    double dy = v.getY() - u.getY();
                    double dist = Math.max(0.01, Math.sqrt(dx * dx + dy * dy));
                    double force = k * k / dist;
                    double[] d = disp.get(v.getId());
                    d[0] += dx / dist * force;
                    d[1] += dy / dist * force;
                }
            }
            for (Connection edge : g.getConnections()) {
                Node v = g.getNode(edge.getFrom());
                Node u = g.getNode(edge.getTo());
                if (v == null || u == null) continue;
                double dx = v.getX() - u.getX();
                double dy = v.getY() - u.getY();
                double dist = Math.max(0.01, Math.sqrt(dx * dx + dy * dy));
                double force = dist * dist / k;
                double[] dv = disp.get(v.getId());
                double[] du = disp.get(u.getId());
                dv[0] -= dx / dist * force;
                dv[1] -= dy / dist * force;
                du[0] += dx / dist * force;
                du[1] += dy / dist * force;
            }
            for (Node v : g.allNodes()) {
                double[] d = disp.get(v.getId());
                double dist = Math.max(0.01, Math.sqrt(d[0] * d[0] + d[1] * d[1]));
                double dx = d[0] / dist * Math.min(dist, temperature);
                double dy = d[1] / dist * Math.min(dist, temperature);
                v.setX(Math.min(width, Math.max(0, v.getX() + dx)));
                v.setY(Math.min(height, Math.max(0, v.getY() + dy)));
            }
            temperature *= 0.95;
        }
    }

    private static void applyChrobakPayne(Graph g, int width, int height) {
        int n = g.getNodes().size();
        if (n == 0) return;
        double centerX = width / 2.0;
        double centerY = height / 2.0;
        double radius = Math.min(width, height) * 0.35;
        int index = 0;
        for (Node node : g.allNodes()) {
            double angle = 2 * Math.PI * index / n;
            node.setX(centerX + radius * Math.cos(angle));
            node.setY(centerY + radius * Math.sin(angle));
            index++;
        }
    }

    private static void runFileLoader(String nodesPath, String connectionsPath) throws Exception {
        Graph g = new Graph();
        g.loadNodesFromTxt(new File(nodesPath));

        List<String> invalidLines = g.loadConnectionsFromTxt(new File(connectionsPath), new ArrayList<>());
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

        System.out.println("No default graph nodes are created.");
        System.out.println("Nodes:");
        for (Node n : g.allNodes()) {
            System.out.println("  " + n);
        }
        System.out.println("Connections:");
        for (Connection c : g.getConnections()) {
            System.out.println("  " + c);
        }
    }

    private static class CliOptions {
        String inputFile;
        String outputFile;
        String algorithm;
        int iterations = 100;
        String format = "txt";
        boolean svg = false;
        boolean help = false;
    }
}
