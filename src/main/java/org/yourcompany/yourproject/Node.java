package org.yourcompany.yourproject;

public class Node {
    private String id;
    private double x;
    private double y;

    public Node() {
    }

    public Node(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Node{" + "id='" + id + '\'' + ", x=" + x + ", y=" + y + '}';
    }
}
