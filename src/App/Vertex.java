package App;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Vertex {
    public static final Vertex NO_PARTICULAR_VERTEX = null;

    // possible improvement - create here field instead of creating wrappers

    private final String name;
    private int coordinatesWeight;
    private Coordinates physicalCoordinates;

    // One vertex has connections to other vertices
    // To each vertex we can get at different times of day (so there are needed multiple edges)
    // At specific time of day only one connection will be the best - with the lowest total cost at given time of day
    private Map<Vertex, EdgesToSameVertex> connections;     // Edge connects with Vertex


    public double accMinutesElapsed;
    public int prevInterchangesCount;
    public Vertex predecessor;
    public Edge linkToPredecessor;
    public boolean interchangesCritter;

    public double h;
    public double g;
    public double f;

    public void setAlgEvaluationFields(
            double accMinutesElapsed,
            int prevInterchangesCount,
            Vertex predecessor,
            Edge linkToPredecessor,
            Vertex dest,
            boolean interchangesCritter
    ) {
        this.accMinutesElapsed = accMinutesElapsed;
        this.prevInterchangesCount = prevInterchangesCount;
        this.predecessor = predecessor;
        this.linkToPredecessor = linkToPredecessor;
        this.interchangesCritter = interchangesCritter;

        this.h = h(dest);
        this.g = Measures.cost(accMinutesElapsed, prevInterchangesCount, interchangesCritter);
        this.f = h + g;
    }

    public void updateSearchVars() {
        this.g = Measures.cost(accMinutesElapsed, prevInterchangesCount, interchangesCritter);
        this.f = h + g;
    }


    public Vertex(String name, Coordinates physicalCoordinates) {
        this.name = name;
        this.coordinatesWeight = 1;
        this.physicalCoordinates = physicalCoordinates.copy();
        this.connections = new HashMap<>();
    }

    public void updateCoordinates(Coordinates toUpdate) {
        double latAcc = this.physicalCoordinates.latitude() * coordinatesWeight + toUpdate.latitude();
        double lonAcc = this.physicalCoordinates.longitude() * coordinatesWeight + toUpdate.longitude();
        this.coordinatesWeight ++;
        this.physicalCoordinates = new Coordinates(latAcc/this.coordinatesWeight, lonAcc/this.coordinatesWeight);
    }

    public void updateEdge(RawRoute outgoingEdge, Map<String, Vertex> existingVertices) {
        boolean doAllDepartureFromThisVertex = outgoingEdge.startLabel().equals(this.name);
        if (!doAllDepartureFromThisVertex)
            throw new RuntimeException("Use of method inappropriate with convention");

        Edge actualEdge = new Edge(outgoingEdge);
        Vertex destVertex = existingVertices.get(outgoingEdge.endLabel());
        if (destVertex == null)
            throw new RuntimeException("Use of method inappropriate with convention");

        if (!connections.containsKey(destVertex))
            connections.put(destVertex, new EdgesToSameVertex());
        EdgesToSameVertex edgesAtDifferentDayTimes = connections.get(destVertex);
        edgesAtDifferentDayTimes.add(actualEdge);
    }

    public void updateEdge(Edge hardConnection, Vertex destVertex) {
        if (!connections.containsKey(destVertex))
            connections.put(destVertex, new EdgesToSameVertex());
        EdgesToSameVertex edgesAtDifferentDayTimes = connections.get(destVertex);
        edgesAtDifferentDayTimes.add(hardConnection);
    }

    public void updateEdges(List<RawRoute> outgoingEdges, Map<String, Vertex> existingVertices) {
        outgoingEdges.forEach(edge->updateEdge(edge, existingVertices));
    }

    public double getPureTimeCostTo(Vertex v, double minuteOfDay) {
        EdgesToSameVertex es = this.connections.get(v);
        Optional<Edge> result = es.getTheBestEdgeWithPureTimeCriteria(minuteOfDay);
        return result.get().getPureMinutesTimeCost(minuteOfDay);
    }

    public Edge getEdgeByPureTimeCostTo(Vertex v, double minuteOfDay) {
        EdgesToSameVertex es = this.connections.get(v);
        Optional<Edge> result = es.getTheBestEdgeWithPureTimeCriteria(minuteOfDay);
        return result.get();
    }

    public Edge getEdgeByInterchangeAndTimeCost(Vertex v, String previousLine, double minuteOfDay){
        EdgesToSameVertex es = this.connections.get(v);
        Optional<Edge> result = es.getTheBestEdgeWithInterchangeAndTimeCriteria(previousLine, minuteOfDay);
        return result.get();
    }

    public double getInterchangeAndTimeCost(Vertex v, String previousLine, double minuteOfDay) {
        EdgesToSameVertex es = this.connections.get(v);
        Optional<Edge> result = es.getTheBestEdgeWithInterchangeAndTimeCriteria(previousLine, minuteOfDay);
        return result.get().getInterchangeAndTimeCost(previousLine, minuteOfDay);
    }

    public Set<Vertex> getNeighbours() {
        return connections.keySet();
    }

    public double g(Vertex v, double minuteOfDay) {
        return getPureTimeCostTo(v, minuteOfDay);
    }

    public Edge gEdge(Vertex v, double minuteOfDay) {
        return getEdgeByPureTimeCostTo(v, minuteOfDay);
    }

    public double g(Vertex v, String previousLine, double minuteOfDay) {
        return getInterchangeAndTimeCost(v, previousLine, minuteOfDay);
    }

    public double h(Vertex target) {
        return Measures.haversine(
                this.physicalCoordinates.latitude(),
                this.physicalCoordinates.longitude(),
                target.physicalCoordinates.latitude(),
                target.physicalCoordinates.longitude()
        );
    }

    public double cost(Vertex dest, String previousLine, double arrivalAtThisVertexTime, boolean interchangeCritter) {
        if (!interchangeCritter)
            return g(dest,arrivalAtThisVertexTime);
        else
            return g(dest,previousLine,arrivalAtThisVertexTime);
    }

    @Override
    public String toString() {
        return String.format("%s",name);
    }

    public String extendedString() {
        return String.format("%s;%s;%s",name,physicalCoordinates.latitude(),physicalCoordinates.longitude());
    }

    public Map<Vertex, EdgesToSameVertex> getConnections(){
        return connections;
    }

    public String getName() {
        return name;
    }


    public Set<String> getLinesOutgoing(Vertex destVertex){
        Stream<Edge> edges = destVertex != null ?
                connections
                        .get(destVertex)
                        .stream() :
                connections
                        .values()
                        .stream()
                        .flatMap(Set::stream);

        return edges.map(Edge::getLineLabel).collect(Collectors.toSet());
    }
}
