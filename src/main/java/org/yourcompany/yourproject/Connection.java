package org.yourcompany.yourproject;

public class Connection {
    private String name;
    private String from;
    private String to;
    private double weight;

    public Connection() {
    }

    public Connection(String from, String to) {
        this(null, from, to, 0.0);
    }

    public Connection(String name, String from, String to, double weight) {
        this.name = name;
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Connection{" + "name='" + name + '\'' + ", from='" + from + '\'' + ", to='" + to + '\'' + ", weight=" + weight + '}';
    }
}
