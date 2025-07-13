package App;

import Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class DijkstraAlgorithm {
    public static class AlgParams {
        public double accTimeCost;
        public Vertex predecessor;
        public Edge linkToPredecessor;

        public AlgParams(){
        }

        public AlgParams(double accTimeCost, Vertex predecessor, Edge linkToPredecessor) {
            this.accTimeCost = accTimeCost;
            this.predecessor = predecessor;
            this.linkToPredecessor = linkToPredecessor;
        }
    }
    private Graph graph;
    private HashMap<Vertex, AlgParams> distancesAndPredecessor;

    private Vertex srcCurrent;
    private Vertex destCurrent;
    private double startMinuteOfDay;

    private List<Vertex> toCheck;


    public DijkstraAlgorithm(Graph graph) {
        this.graph = graph;
        this.distancesAndPredecessor = new HashMap<>(graph.getVertices().size());

        this.srcCurrent = null;

        this.toCheck = new ArrayList<>(graph.getVertices().size());
    }

    public void prepare(Vertex src, Vertex dest, double startMinuteOfDay) {
        this.srcCurrent = src;
        this.destCurrent = dest;
        this.startMinuteOfDay = startMinuteOfDay;
    }

    private void initialize(){
        this.distancesAndPredecessor.clear();
        for (Vertex vertex : this.graph.getVertices()) {
            this.distancesAndPredecessor.put(
                    vertex,
                    new AlgParams(
                        Integer.MAX_VALUE,
                        null,
                        null
                    )
            );
        }
        AlgParams sourceParams = this.distancesAndPredecessor.get(this.srcCurrent);
        sourceParams.accTimeCost = 0.0;
    }

    public Vertex getMinVertex(){
        return this.toCheck.stream().min((o1, o2) -> {
            var o1P = distancesAndPredecessor.get(o1);
            var o2P = distancesAndPredecessor.get(o2);

            return Double.compare(o1P.accTimeCost, o2P.accTimeCost);
        }).get();
    }

    public void calculateDistances(){
        if (this.graph.getVertices().isEmpty())
            return;
        initialize();
        this.toCheck.clear();
        this.toCheck.addAll(this.graph.getVertices());

        while (!toCheck.isEmpty()) {
            Vertex vCurrent = getMinVertex();
            toCheck.remove(vCurrent);
            if (vCurrent == this.destCurrent) {
                return;
            }

            var vCurrentParams = distancesAndPredecessor.get(vCurrent);

            Set<Vertex> vNeighbours = vCurrent.getNeighbours();
            for (Vertex vN : vNeighbours) {
                double stationArrivalTime = Utils.calcMinuteOfDay(
                        startMinuteOfDay,
                        vCurrentParams.accTimeCost
                );
                Edge transitionLink = vCurrent.gEdge(
                        vN,
                        stationArrivalTime
                );
                double timeDistance = transitionLink.getPureMinutesTimeCost(stationArrivalTime);

                AlgParams vNParams = distancesAndPredecessor.get(vN);

                if (vNParams.accTimeCost > timeDistance + vCurrentParams.accTimeCost){
                    vNParams.accTimeCost = timeDistance + vCurrentParams.accTimeCost;
                    vNParams.predecessor = vCurrent;
                    vNParams.linkToPredecessor = transitionLink;
                }
            }
        }
    }

    public ConstructedPath constructPathToDest() {
        ConstructedPath result = new ConstructedPath(destCurrent, startMinuteOfDay);

        List<ConstructedPath.StationAndConnection> phases = new ArrayList<>();

        AlgParams current = distancesAndPredecessor.get(destCurrent);
        while (current.predecessor != null) {
            phases.add(
                    0,
                    new ConstructedPath.StationAndConnection(
                        current.predecessor,
                        current.linkToPredecessor
                    )
            );
            current = distancesAndPredecessor.get(current.predecessor);
        }
        result.setRoutePhases(phases);

        return result;
    }
}
