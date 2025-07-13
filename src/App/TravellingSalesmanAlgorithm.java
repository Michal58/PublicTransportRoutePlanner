package App;

import Utils.Utils;

import java.util.*;

import static Utils.Utils.getLast;
import static Utils.Utils.randomizer;

public class TravellingSalesmanAlgorithm {
    public static int STEP_LIMIT = 1000;
    public static int LOCAL_OPT_LIMIT = 10000;

    public static final int MIN_TABU_SIZE = 5;
    public static final double TABU_SIZE_FACTOR = 0.3;

    public static final double ASPIRATION_FACTOR = 0.1;

    public static final double TO_PROBE_FACTOR = 0.3;

    private Graph graph;

    private boolean interchangesCritter;

    private Vertex src;
    private List<Vertex> listToMeet;
    private double startMinuteOfDay;

    public TravellingSalesmanAlgorithm(Graph graph){
        this.graph = graph;
    }

    public void prepare(Vertex src, List<Vertex> listToMeet, double startMinuteOfDay, boolean interchangesCritter){
        this.src = src;
        this.listToMeet = listToMeet;
        this.startMinuteOfDay = startMinuteOfDay;
        this.interchangesCritter = interchangesCritter;
    }

    public ConstructedPath constructPath() {
        return constructPath(listToMeet);
    }

    public ConstructedPath constructPath(List<Vertex> listToMeet) {
        AStarAlgorithm aStarSolver = new AStarAlgorithm(graph);
        Vertex currentSrc = src;
        double currentStartMinuteOfDay = startMinuteOfDay;

        String currentLine = null;

        ConstructedPath globalPath = null;

        for (Vertex currentDest : listToMeet) {
            aStarSolver.prepare(currentSrc, currentDest, currentStartMinuteOfDay, interchangesCritter);
            aStarSolver.calculateDistancesWithSetLine(currentLine);
            ConstructedPath currentPath = aStarSolver.constructPathToDest();

            if (globalPath == null) {
                globalPath = currentPath;
            }
            else {
                globalPath.routePhases.addAll(currentPath.routePhases);
                globalPath.destStation = currentPath.destStation;
            }
            currentStartMinuteOfDay = getLast(currentPath.routePhases).connection.getNextStationArrivalTime();

            currentSrc = currentDest;
            currentLine = getLast(currentPath.routePhases).connection.getLineLabel();
        }

        return globalPath;
    }

    public static class TabuResult {
        public int swapFirstIndex = -1;
        public int swapSecondIndex = -1;

        public double exactEval = Double.MAX_VALUE;
        public double approximatedEvaluation;
        public Vertex[] actualSolution;
        public TabuResult(double approximatedEvaluation, Vertex[] actualSolution){
            this.approximatedEvaluation = approximatedEvaluation;
            this.actualSolution = actualSolution;
        }
        public TabuResult(double approximatedEvaluation, List<Vertex> solutions){
            this.approximatedEvaluation = approximatedEvaluation;
            this.actualSolution = solutions.toArray(new Vertex[0]);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualSolution);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof TabuResult) && Arrays.equals(((TabuResult) obj).actualSolution,actualSolution);
        }

        @Override
        public String toString() {
            return Arrays.toString(actualSolution);
        }
    }

    public static class IndexPair {
        public int firstI;
        public int secondI;
        public IndexPair(int firstI, int secondI) {
            this.firstI = firstI;
            this.secondI = secondI;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            IndexPair indexPair = (IndexPair) o;
            return firstI == indexPair.firstI && secondI == indexPair.secondI;
        }

        @Override
        public int hashCode() {
            return Objects.hash(firstI, secondI);
        }

        @Override
        public String toString() {
            return "IndexPair{" +
                    "firstI=" + firstI +
                    ", secondI=" + secondI +
                    '}';
        }
    }

    public double approximate(TabuResult result){
        double accDistanceApproximation = 0;
        for (int i = 0; i < result.actualSolution.length - 1; i++) {
            Vertex currentSrc = result.actualSolution[i];
            Vertex currentDest = result.actualSolution[i+1];

            accDistanceApproximation += currentSrc.h(currentDest);
        }

        return accDistanceApproximation;
    }

    public int getSizeOfPairsSet() {
        int dimsCount = listToMeet.size();
        return (dimsCount * (dimsCount - 1)) / 2;
    }

    private Set<TabuResult> generateBaseNeighborhood(TabuResult s) {
        int dimsCount = listToMeet.size();
        Set<TabuResult> neighborhood = new HashSet<>(getSizeOfPairsSet());

        for (int i = 0; i < dimsCount; i++) {
            for (int j = i + 1; j < dimsCount; j++) {
                TabuResult neighbour = new TabuResult(-1,Arrays.copyOf(s.actualSolution,listToMeet.size()));
                Utils.swap(neighbour.actualSolution, i, j);
                neighbour.swapFirstIndex = i;
                neighbour.swapSecondIndex = j;
                neighbour.approximatedEvaluation = approximate(neighbour);

                neighborhood.add(neighbour);
            }
        }

        return neighborhood;
    }

    private void calculateBase() {
        int k = 0;
        TabuResult optS = new TabuResult(-1,listToMeet);
        optS.approximatedEvaluation = approximate(optS);

        TabuResult currentS = optS;

        Set<TabuResult> tabu = new HashSet<>();

        while (k < STEP_LIMIT) {
            int i = 0;
            boolean isSenseToIterate = true;

            while (i < LOCAL_OPT_LIMIT && isSenseToIterate) {
                Set<TabuResult> nS = generateBaseNeighborhood(currentS);
                TabuResult sPrim = nS
                        .stream()
                        .filter(oneOfNS-> !tabu.contains(oneOfNS))
                        .min(Comparator.comparingDouble(o -> o.approximatedEvaluation))
                        .orElse(currentS);

                tabu.addAll(nS);
                if (sPrim.approximatedEvaluation < currentS.approximatedEvaluation) {
                    currentS = sPrim;
                }
                else
                    isSenseToIterate = false;

                i++;
            }
            k++;
            if (currentS.approximatedEvaluation < optS.approximatedEvaluation) {
                optS = currentS;
            }
        }

        listToMeet = Arrays.asList(optS.actualSolution);
    }

    private void calculateTabuConstrained() {
        int k = 0;
        TabuResult optS = new TabuResult(-1,listToMeet);
        optS.approximatedEvaluation = approximate(optS);

        TabuResult currentS = optS;

        Queue<IndexPair> tabu = new LinkedList<>();
        int tabuLimit = (int) (listToMeet.size() * TABU_SIZE_FACTOR) + MIN_TABU_SIZE;

        while (k < STEP_LIMIT) {
            int i = 0;
            boolean isSenseToIterate = true;

            while (i < LOCAL_OPT_LIMIT && isSenseToIterate) {
                Set<TabuResult> nS = generateBaseNeighborhood(currentS);

                TabuResult sPrim = nS
                        .stream()
                        .filter(oneOfNS-> !tabu.contains(new IndexPair(oneOfNS.swapFirstIndex, oneOfNS.swapSecondIndex)))
                        .min(Comparator.comparingDouble(o -> o.approximatedEvaluation))
                        .orElse(currentS);

                tabu.add(new IndexPair(sPrim.swapFirstIndex,sPrim.swapSecondIndex));
                if (tabu.size() > tabuLimit) {
                    tabu.poll();
                }

                if (sPrim.approximatedEvaluation < currentS.approximatedEvaluation) {
                    currentS = sPrim;
                }
                else
                    isSenseToIterate = false;
                i++;
            }
            k++;
            if (currentS.approximatedEvaluation < optS.approximatedEvaluation) {
                optS = currentS;
            }
        }

        listToMeet = Arrays.asList(optS.actualSolution);
    }

    public boolean aspirationAllow(TabuResult currentS, TabuResult aspirating){
        return (1 + ASPIRATION_FACTOR) * aspirating.approximatedEvaluation < currentS.approximatedEvaluation;
    }

    private void calculateTabuAspiration(){
        int k = 0;
        TabuResult optS = new TabuResult(-1,listToMeet);
        optS.approximatedEvaluation = approximate(optS);

        TabuResult currentS = optS;

        Queue<IndexPair> tabu = new LinkedList<>();
        int tabuLimit = (int) (listToMeet.size() * TABU_SIZE_FACTOR) + MIN_TABU_SIZE;

        while (k < STEP_LIMIT) {
            int i = 0;
            boolean isSenseToIterate = true;

            while (i < LOCAL_OPT_LIMIT && isSenseToIterate) {
                Set<TabuResult> nS = generateBaseNeighborhood(currentS);
                TabuResult sPhantom = currentS;

                TabuResult sPrim = nS
                        .stream()
                        .filter(oneOfNS->
                                !tabu.contains(
                                        new IndexPair(oneOfNS.swapFirstIndex, oneOfNS.swapSecondIndex)
                                ) || aspirationAllow(sPhantom,oneOfNS)
                        )
                        .min(Comparator.comparingDouble(o -> o.approximatedEvaluation))
                        .orElse(currentS);

                tabu.add(new IndexPair(sPrim.swapFirstIndex,sPrim.swapSecondIndex));
                if (tabu.size() > tabuLimit) {
                    tabu.poll();
                }

                if (sPrim.approximatedEvaluation < currentS.approximatedEvaluation) {
                    currentS = sPrim;
                }
                else
                    isSenseToIterate = false;
                i++;
            }
            k++;
            if (currentS.approximatedEvaluation < optS.approximatedEvaluation) {
                optS = currentS;
            }
        }

        listToMeet = Arrays.asList(optS.actualSolution);
    }

    private Set<TabuResult> generateProbedNeighborhood(TabuResult s) {
        int dimsCount = listToMeet.size();
        int toGenerate = (int)(getSizeOfPairsSet()*TO_PROBE_FACTOR);
        Set<TabuResult> neighborhood = new HashSet<>(toGenerate);

        for (int i = 0; i < toGenerate; i++) {
            int randI = -1;
            int randJ = -1;
            while (randI >= randJ) {
                randI = randomizer.nextInt(0, dimsCount);
                randJ = randomizer.nextInt(0, dimsCount);
            }

            TabuResult neighbour = new TabuResult(-1,Arrays.copyOf(s.actualSolution,listToMeet.size()));
            Utils.swap(neighbour.actualSolution, randI, randJ);
            neighbour.swapFirstIndex = randI;
            neighbour.swapSecondIndex = randJ;
            neighbour.approximatedEvaluation = approximate(neighbour);

            neighborhood.add(neighbour);
        }

        return neighborhood;
    }

    private void calculateWithProbes(){
        int k = 0;
        TabuResult optS = new TabuResult(-1,listToMeet);
        optS.approximatedEvaluation = approximate(optS);

        TabuResult currentS = optS;

        Queue<IndexPair> tabu = new LinkedList<>();
        int tabuLimit = (int) (listToMeet.size() * TABU_SIZE_FACTOR) + MIN_TABU_SIZE;

        while (k < STEP_LIMIT) {
            int i = 0;
            boolean isSenseToIterate = true;

            while (i < LOCAL_OPT_LIMIT && isSenseToIterate) {
                Set<TabuResult> nS = generateProbedNeighborhood(currentS);
                TabuResult sPhantom = currentS;

                TabuResult sPrim = nS
                        .stream()
                        .filter(oneOfNS->
                                !tabu.contains(
                                        new IndexPair(oneOfNS.swapFirstIndex, oneOfNS.swapSecondIndex)
                                ) || aspirationAllow(sPhantom,oneOfNS)
                        )
                        .min(Comparator.comparingDouble(o -> o.approximatedEvaluation))
                        .orElse(currentS);

                tabu.add(new IndexPair(sPrim.swapFirstIndex,sPrim.swapSecondIndex));
                if (tabu.size() > tabuLimit) {
                    tabu.poll();
                }

                if (sPrim.approximatedEvaluation < currentS.approximatedEvaluation) {
                    currentS = sPrim;
                }
                else
                    isSenseToIterate = false;
                i++;
            }
            k++;
            if (currentS.approximatedEvaluation < optS.approximatedEvaluation) {
                optS = currentS;
            }
        }

        listToMeet = Arrays.asList(optS.actualSolution);
    }

    public static final int TURNED_OFF = 0;
    public static final int BASE_ALGORITHM = 1;
    public static final int TABU_CONSTRAINED = 2;
    public static final int ASPIRATION_BASED = 3;
    public static final int PROBE_BASED = 4;

    public static final int ALG_VERSION = PROBE_BASED;

    public void calculateDistances() {
        if (ALG_VERSION == TURNED_OFF)
            return;
        else if (ALG_VERSION == BASE_ALGORITHM) {
            calculateBase();
        } else if (ALG_VERSION == TABU_CONSTRAINED) {
            calculateTabuConstrained();
        } else if (ALG_VERSION == ASPIRATION_BASED) {
            calculateTabuAspiration();
        } else if (ALG_VERSION == PROBE_BASED)
            calculateWithProbes();
    }
}
