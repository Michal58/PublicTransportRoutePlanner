package App;

import Utils.Utils;
import Utils.StdPair;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResultsGetter {
    public static final String SEP = "=".repeat(50);

    private Graph currentGraph;
    private Class<?> currentAlgorithmDisc;
    private String currentStartStation;
    private String currentDestStation;
    private LocalTime currentStartTime;


    private Vertex startV;
    private Vertex destV;
    private Object usedAlgorithm;

    private boolean interchangesCritter;

    private List<String> currentListToMeet;

    private List<Vertex> vList;

    public ResultsGetter(){
    }

    private boolean invalidStartVProc(){
        System.out.println("Invalid start station");
        System.out.println(SEP);
        return false;
    }

    public boolean prepareInitialization(){
        System.out.println(SEP);

        if (startV == null) {
            return invalidStartVProc();
        }
        if (destV == null) {
            System.out.println("Invalid dest station");
            System.out.println(SEP);
            return false;
        }
        System.out.println("From: " + startV.getName());
        System.out.println("To: " + destV.getName());
        System.out.println("Starting at: " + currentStartTime);

        System.out.println("Start of calculation\n");
        return true;
    }

    public boolean prepareTSInitialization() {
        System.out.println(SEP);
        if (startV == null) {
            return invalidStartVProc();
        }
        if (vList.stream().anyMatch(Objects::isNull)) {
            System.out.println("Invalid list of stations");
            System.out.println(SEP);
            return false;
        }

        System.out.println("From: " + startV.getName());
        System.out.println("Including: " + vList);
        System.out.println("Starting at: " + currentStartTime);

        System.out.println("Start of calculation\n");
        return true;
    }

    private Object prepareAlgorithmForActualCalculations() {
        if (currentAlgorithmDisc.equals(DijkstraAlgorithm.class)){
            DijkstraAlgorithm da = new DijkstraAlgorithm(currentGraph);
            da.prepare(
                    startV,
                    destV,
                    Utils.localTimeToMinuteOfDay(currentStartTime)
            );
            return da;
        } else if (currentAlgorithmDisc.equals(AStarAlgorithm.class)) {
            AStarAlgorithm aStar = new AStarAlgorithm(currentGraph);
            aStar.prepare(
                    startV,
                    destV,
                    Utils.localTimeToMinuteOfDay(currentStartTime),
                    interchangesCritter
            );
            return aStar;
        } else if (currentAlgorithmDisc.equals(TravellingSalesmanAlgorithm.class)) {
            TravellingSalesmanAlgorithm tsa = new TravellingSalesmanAlgorithm(currentGraph);
            tsa.prepare(
                    startV,
                    vList,
                    Utils.localTimeToMinuteOfDay(currentStartTime),
                    interchangesCritter
            );
            return tsa;
        }

        throw new RuntimeException("Algorithm preparation is not exhaustive");
    }

    private void performAlgorithmCalculations() {
        if (currentAlgorithmDisc.equals(DijkstraAlgorithm.class)) {
            ((DijkstraAlgorithm) usedAlgorithm).calculateDistances();
            return;
        } else if (currentAlgorithmDisc.equals(AStarAlgorithm.class)) {
            ((AStarAlgorithm) usedAlgorithm).calculateDistances();
            return;
        } else if (currentAlgorithmDisc.equals(TravellingSalesmanAlgorithm.class)) {
            ((TravellingSalesmanAlgorithm) usedAlgorithm).calculateDistances();
            return;
        }

        throw new RuntimeException("Calculations are not exhaustive");
    }

    private ConstructedPath getPathFromAlgorithm(){
        if (currentAlgorithmDisc.equals(DijkstraAlgorithm.class)) {
            return ((DijkstraAlgorithm) usedAlgorithm).constructPathToDest();
        } else if (currentAlgorithmDisc.equals(AStarAlgorithm.class)) {
            return ((AStarAlgorithm) usedAlgorithm).constructPathToDest();
        } else if (currentAlgorithmDisc.equals(TravellingSalesmanAlgorithm.class)) {
            return ((TravellingSalesmanAlgorithm) usedAlgorithm).constructPath();
        }

        throw new RuntimeException("Path getting is not exhaustive");
    }

    private StdPair<Double,ConstructedPath> conductCalculationsAndMeasurements(){
        usedAlgorithm = prepareAlgorithmForActualCalculations();

        long sT = System.currentTimeMillis();

        performAlgorithmCalculations();
        ConstructedPath calculatedPath = getPathFromAlgorithm();

        long eT = System.currentTimeMillis();
        return new StdPair<>((double) (eT - sT) / 1000, calculatedPath);
    }

    public void sumUpTimeCostResults() {
        startV = currentGraph.getVertexWithLabel(currentStartStation);
        destV = currentGraph.getVertexWithLabel(currentDestStation);

        boolean wasSuccess = prepareInitialization();
        if (!wasSuccess)
            return;

        StdPair<Double,ConstructedPath> calculationsAndMeasures = conductCalculationsAndMeasurements();
        double dT = calculationsAndMeasures.v1;

        System.out.println(calculationsAndMeasures.v2.sumUpSchedule());
        System.out.println(calculationsAndMeasures.v2.sumUpMacroParameters(interchangesCritter));
        System.out.printf("Calculation time spent: %.4f%n", dT);

        System.out.println(SEP);
    }

    public void sumUpTSCostResults() {
        startV = currentGraph.getVertexWithLabel(currentStartStation);
        vList = currentListToMeet
                .stream()
                .map(s -> currentGraph.getVertexWithLabel(s))
                .toList();

        boolean wasSuccess = prepareTSInitialization();
        if (!wasSuccess)
            return;

        StdPair<Double,ConstructedPath> calculationsAndMeasures = conductCalculationsAndMeasurements();
        double dT = calculationsAndMeasures.v1;

        Set<Vertex> interestVertices = new HashSet<>(vList);
        interestVertices.add(startV);

        System.out.println(calculationsAndMeasures.v2.sumUpSchedule(interestVertices));
        System.out.println(calculationsAndMeasures.v2.sumUpMacroParameters(interchangesCritter));
        System.out.printf("Calculation time spent: %.4f%n", dT);

        System.out.println(SEP);
    }

    public void runSummary(Object[][] argsSet){
        for (var args : argsSet){
            this.currentGraph = (Graph) args[0];
            this.currentStartStation = (String) args[1];
            this.currentDestStation = (String) args[2];
            this.currentStartTime = (LocalTime) args[3];

            sumUpTimeCostResults();
        }
    }

    public void runTSSummary(Object[][] argsSet) {
        for (var args : argsSet){
            this.currentGraph = (Graph) args[0];
            this.currentStartStation = (String) args[1];
            this.currentListToMeet = (List<String>) args[2];
            this.currentStartTime = (LocalTime) args[3];

            sumUpTSCostResults();
        }
    }

    public void prepareResultsForDijkstraWithParameters(Object[][] argsSet){
        interchangesCritter = false;
        currentAlgorithmDisc = DijkstraAlgorithm.class;
        runSummary(argsSet);
    }

    public void prepareResultsForAStarAlgorithmWithPureTime(Object[][] argsSet){
        interchangesCritter = false;
        currentAlgorithmDisc = AStarAlgorithm.class;
        runSummary(argsSet);
    }

    public void prepareResultsForAStartAlgorithmWithInterchanges(Object[][] argsSet) {
        interchangesCritter = true;
        currentAlgorithmDisc = AStarAlgorithm.class;
        runSummary(argsSet);
    }

    public void prepareResultsForTravellingSalesmanWithPureTime(Object[][] argSet) {
        interchangesCritter = false;
        currentAlgorithmDisc = TravellingSalesmanAlgorithm.class;
        runTSSummary(argSet);
    }

    public void prepareResultsForTravellingSalesmanWithInterchanges(Object[][] argSet) {
        interchangesCritter = true;
        currentAlgorithmDisc = TravellingSalesmanAlgorithm.class;
        runTSSummary(argSet);
    }
}
