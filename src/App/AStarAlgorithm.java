package App;

import Utils.Utils;

import java.util.*;

import static App.Measures.cost;

public class AStarAlgorithm {
    private Graph graph;
    private TreeSet<Vertex> openedVertices;

    private HashSet<Vertex> closedVertices;


    private boolean interchangesCritter;

    private Vertex src;
    private Vertex dest;
    private double startMinuteOfDay;

    private ConstructedPath shortestPath;


    public AStarAlgorithm(Graph graph){
        this.graph = graph;

        Comparator<Vertex> vertexComparator = Comparator
                .comparingDouble((Vertex v) -> v.f)
                .thenComparing(Vertex::hashCode);

        this.openedVertices = new TreeSet<>(vertexComparator);
        this.closedVertices = new HashSet<>();

        shortestPath = null;
    }

    public void prepare(Vertex src, Vertex dest, double startMinuteOfDay, boolean interchangesCritter) {
        this.src = src;
        this.dest = dest;
        this.startMinuteOfDay = startMinuteOfDay;
        this.interchangesCritter = interchangesCritter;
    }

    private void initialize(){
        src.setAlgEvaluationFields(
                0,
                0,
                null,
                null,
                dest,
                interchangesCritter
        );

        this.openedVertices.clear();
        this.openedVertices.add(src);
        this.closedVertices.clear();
    }

    private void updatePositionInOpened(Vertex v){
        openedVertices.remove(v);
        openedVertices.add(v);
    }

    private void updateGDist(
            Vertex current,
            Vertex toUpdate,
            Edge transitionEdge
    ){
        double transitionTimeElapsed = transitionEdge.getPureMinutesTimeCost(
                Utils.calcMinuteOfDay(startMinuteOfDay, current.accMinutesElapsed)
        );

        toUpdate.accMinutesElapsed = current.accMinutesElapsed + transitionTimeElapsed;
        toUpdate.prevInterchangesCount = current.prevInterchangesCount + Utils.wasInterchange(
                current.linkToPredecessor.getLineLabel(),
                transitionEdge.getLineLabel()
        );
        toUpdate.predecessor = current;
        toUpdate.linkToPredecessor = transitionEdge;

        toUpdate.updateSearchVars();
    }

    private void calculateDistances(String startLine){
        src.linkToPredecessor = Edge.createLabelledEdge(startLine);

        if (openedVertices.isEmpty())
            throw new RuntimeException("No vertices to check");

        while (!this.openedVertices.isEmpty()){
            Vertex currentNode = openedVertices.pollFirst();

            if (currentNode == dest){
                return;
            }

            closedVertices.add(currentNode);

            for (Vertex vN : currentNode.getNeighbours()) {
                vN.interchangesCritter = interchangesCritter;

                double stationArrivalTime = Utils.calcMinuteOfDay(
                        startMinuteOfDay,
                        currentNode.accMinutesElapsed
                );

                Edge currentTransition =
                        interchangesCritter ?
                        currentNode.getEdgeByInterchangeAndTimeCost(
                                vN,
                                currentNode.linkToPredecessor.getLineLabel(),
                                stationArrivalTime
                        ):
                        currentNode.gEdge(
                                vN,
                                stationArrivalTime
                        );

                double transitionTimeElapsed = currentTransition.getPureMinutesTimeCost(stationArrivalTime);
                int transitionCountOfInterchanges = Utils.wasInterchange(
                        currentNode.linkToPredecessor.getLineLabel(),
                        currentTransition.getLineLabel()
                );

                double transitionCost = cost(transitionTimeElapsed,transitionCountOfInterchanges,interchangesCritter);

                if (!openedVertices.contains(vN) && !closedVertices.contains(vN)) {
                    updateGDist(
                            currentNode,
                            vN,
                            currentTransition
                    );
                    openedVertices.add(vN);
                } else if (vN.g > currentNode.g + transitionCost){
                    updateGDist(
                            currentNode,
                            vN,
                            currentTransition
                    );

                    if (closedVertices.contains(vN)){
                        openedVertices.add(vN);
                        closedVertices.remove(vN);
                    }

                    updatePositionInOpened(vN);
                }
            }
        }
    }

    private void conductAlgRun(String lineLabel, Utils.RefMedium<ConstructedPath> currentPath){
        initialize();
        calculateDistances(lineLabel);
        ConstructedPath calculatedPath = constructPathToDest();
        double pathCost = calculatedPath.calculateMacroParameters(interchangesCritter).totalCost();

        if (currentPath.v == null || currentPath.v.calculateMacroParameters(interchangesCritter).totalCost() > pathCost){
            currentPath.v = calculatedPath;
        }
    }

    public void calculateDistancesWithSetLine(String lineToSet) {
        if (lineToSet == null) {
            calculateDistances();
        } else {
            Utils.RefMedium<ConstructedPath> currentPath = new Utils.RefMedium<>(null);
            conductAlgRun(lineToSet, currentPath);
            this.shortestPath = currentPath.v;
        }
    }

    public void calculateDistances() {
        Utils.RefMedium<ConstructedPath> currentPath = new Utils.RefMedium<>(null);
        if (this.interchangesCritter) {
            Set<String> startStationLines = src.getLinesOutgoing(Vertex.NO_PARTICULAR_VERTEX);
            startStationLines.forEach(lineLabel -> {
                conductAlgRun(lineLabel,currentPath);
            });
        }
        else
            conductAlgRun(null,currentPath);

        this.shortestPath = currentPath.v;
    }

    public ConstructedPath constructPathToDest() {
        return ConstructedPath.constructPath(dest,startMinuteOfDay);
    }
}

