package App;

import Utils.Utils;

import java.time.LocalTime;
import java.util.*;

import static Utils.Utils.FULL_DAY_MINUTES;

public class Graph {

    private Map<String, Vertex> existingVertices;
    public Graph(List<RawRoute> initialRoutes) {
        constructGraph(initialRoutes);
    }

    public Vertex updateWithCoordinatesAndCreateIfVertexNotExisting(String label, Coordinates coordinates) {
        Vertex another;
        if (!this.existingVertices.containsKey(label)) {
            another = new Vertex(label, coordinates);
            existingVertices.put(label, another);
        }
        else {
            another = this.existingVertices.get(label);
            another.updateCoordinates(coordinates);
        }
        return another;
    }

    public void constructGraph(List<RawRoute> rawRoutes) {
        this.existingVertices = new HashMap<>(rawRoutes.size());
        for (RawRoute currentRoute : rawRoutes) {
            String startVertexLabel = currentRoute.startLabel();
            Coordinates startVertexCoordinates = currentRoute.startCoordinates();

            Vertex start = updateWithCoordinatesAndCreateIfVertexNotExisting(startVertexLabel, startVertexCoordinates);

            String endVertexLabel = currentRoute.endLabel();
            Coordinates endVertexCoordinates = currentRoute.endCoordinates();

            updateWithCoordinatesAndCreateIfVertexNotExisting(endVertexLabel, endVertexCoordinates);

            start.updateEdge(currentRoute, this.existingVertices);
        }
    }

    public Collection<Vertex> getVertices(){
        return this.existingVertices.values();
    }

    public Vertex getVertexWithLabel(String label){
        return existingVertices.get(label);
    }

    public String printPath(List<Vertex> vertices, double minuteOfDay, boolean isInterchangeCriteria) {
        if (vertices.size()<=1)
            return "No valid path";

        StringBuilder builder = new StringBuilder();
        Iterator<Vertex> vIterator = vertices.iterator();
        Vertex previous = vIterator.next();
        Vertex current = null;

        String previousLine = null;

        while (vIterator.hasNext()){
            current = vIterator.next();
            Edge e;
            if (!isInterchangeCriteria) {
                e = previous.getEdgeByPureTimeCostTo(current, minuteOfDay);
            } else {
                e = previous.getEdgeByInterchangeAndTimeCost(current, previousLine, minuteOfDay);
            }

            builder.append(String.format(
                    "From: %-30s - %4s%6s                  To: %-30s%6s%n",
                    previous.getName(),
                    e.getLineLabel(),
                    e.getStrDepartureMinuteOfDay(),
                    current.getName(),
                    e.getStrNextStationArrivalTime()
            ));

            minuteOfDay += e.getPureMinutesTimeCost(minuteOfDay);
            minuteOfDay %= FULL_DAY_MINUTES;
            previousLine = e.getLineLabel();
            previous = current;
        }
        builder.append(String.format("Dest: %-30s",current.getName())).append('\n');
        return builder.toString();
    }

    public static class StationInPathInfo {
        public Vertex fromVertex;
        public String byLine;
        public LocalTime departureTime;

        public Vertex toVertex;
        public LocalTime arrivalTime;
    }

    public Measures.PathMeasures calculatePathMeasures(List<Vertex> path, double minuteOfDay, boolean interchangeCritter, Utils.RefMedium<List<StationInPathInfo>> eachStationInfo){
        eachStationInfo.v = new ArrayList<>();

        double timeElapsed = 0.0;
        int interchangesCount = 0;

        Iterator<Vertex> vIterator = path.iterator();
        Vertex previous = vIterator.next();
        Vertex current;

        String previousLine = null;

        while (vIterator.hasNext()){
            StationInPathInfo nextPathInfo = new StationInPathInfo();

            current = vIterator.next();

            Edge e;
            if (!interchangeCritter) {
                e = previous.getEdgeByPureTimeCostTo(current, minuteOfDay);
            } else {
                e = previous.getEdgeByInterchangeAndTimeCost(current, previousLine, minuteOfDay);
            }

            nextPathInfo.fromVertex = previous;
            nextPathInfo.toVertex = current;
            nextPathInfo.departureTime = LocalTime.ofSecondOfDay((long) Utils.secondOfDay(minuteOfDay));

            double currentTimeElapsed = e.getPureMinutesTimeCost(minuteOfDay);
            timeElapsed += currentTimeElapsed;
            minuteOfDay += currentTimeElapsed;
            minuteOfDay %= FULL_DAY_MINUTES;
            nextPathInfo.arrivalTime = LocalTime.ofSecondOfDay((long) Utils.secondOfDay(minuteOfDay));

            interchangesCount += previousLine != null && !previousLine.equals(e.getLineLabel()) ? 1 : 0;

            previousLine = e.getLineLabel();
            nextPathInfo.byLine = previousLine;

            previous = current;
            eachStationInfo.v.add(nextPathInfo);
        }

        return new Measures.PathMeasures(
                path,
                Measures.cost(timeElapsed,interchangesCount,interchangeCritter),
                timeElapsed,
                interchangesCount
        );
    }
}
