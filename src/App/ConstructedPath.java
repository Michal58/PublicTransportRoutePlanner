package App;

import Utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConstructedPath {
    public static boolean SQUEEZE_OUTPUT = true;
    public static String INTEREST_MARK = "  <";
    public static class StationAndConnection{
        public Vertex station;
        public Edge connection;
        public StationAndConnection(Vertex station, Edge connection) {
            this.station = station;
            this.connection = connection;
        }
    }
    public record MacroParametersSummary(double minutesElapsed, int interchangesCount, double totalCost) {
    }

    public double forStartingMinuteOfDay;
    public Vertex destStation;
    public List<StationAndConnection> routePhases;

    public ConstructedPath(Vertex destStation, double forStartingMinuteOfDay) {
        this.forStartingMinuteOfDay = forStartingMinuteOfDay;
        this.destStation = destStation;
        this.routePhases = new ArrayList<>();
    }

    public void setRoutePhases(List<StationAndConnection> routePhases) {
        this.routePhases = routePhases;
    }

    public String sumUpSchedule() {
        return sumUpSchedule(Set.of());
    }

    public void sumUpExtended(StringBuilder builder, StationAndConnection[] arrayStationsAndConnections, Set<Vertex> interestPoints){
        for (int i = 0; i < arrayStationsAndConnections.length; i++) {
            StationAndConnection phase = arrayStationsAndConnections[i];

            String pathLine = String.format(
                    "From: %-30s - %4s%6s                  To: %-30s%6s%s%n",
                    phase.station.getName(),
                    phase.connection.getLineLabel(),
                    Utils.timeOfDayFrom(phase.connection.getDepartureMinuteOfDay()),
                    i < arrayStationsAndConnections.length - 1 ?
                            arrayStationsAndConnections[i+1].station.getName() : destStation.getName(),
                    Utils.timeOfDayFrom(phase.connection.getNextStationArrivalTime()),
                    interestPoints.contains(phase.station)?
                            INTEREST_MARK : ""
            );

            interestPoints.remove(phase.station);

            builder.append(pathLine);
        }
    }

    public StationAndConnection[] squeezeStationsAndConnections(StationAndConnection[] base, Set<Vertex> interestPoints) {
        StationAndConnection current = null;

        interestPoints = new HashSet<>(interestPoints);

        List<StationAndConnection> result = new ArrayList<>();


        for (StationAndConnection s : base) {
            boolean shouldCreateNewS = current == null
                    || interestPoints.contains(s.station)
                    || !current.connection.getLineLabel().equals(s.connection.getLineLabel());

            if (shouldCreateNewS) {
                StationAndConnection newS = new StationAndConnection(
                        s.station,
                        Edge.createCustomEdge(
                                s.connection.getLineLabel(),
                                s.connection.getDepartureMinuteOfDay(),
                                (int) s.connection.getNextStationArrivalTime()
                        )
                );
                result.add(newS);
                current = newS;
            } else {
                current.connection.setNextStationArrivalTime((int) s.connection.getNextStationArrivalTime());
            }

            interestPoints.remove(s.station);
        }

        return result.toArray(new StationAndConnection[0]);
    }

    public String sumUpSchedule(Set<Vertex> interestPoints) {
        interestPoints = new HashSet<>(interestPoints);
        if (routePhases.isEmpty())
            return "No valid path";

        StringBuilder builder = new StringBuilder();
        StationAndConnection[] arrayStationsAndConnections = routePhases.toArray(new StationAndConnection[0]);

        if (!SQUEEZE_OUTPUT)
            sumUpExtended(builder, arrayStationsAndConnections, interestPoints);
        else
            sumUpExtended(builder, squeezeStationsAndConnections(arrayStationsAndConnections, interestPoints), interestPoints);


        builder.append(String.format("Dest: %-30s%n",destStation.getName())).append('\n').append('\n');
        builder.append('\n');

        return builder.toString();
    }

    public MacroParametersSummary calculateMacroParameters(boolean interchangesCritter) {
        double timeElapsed = 0;
        int interchangesCount = 0;
        String currentLine = null;

        for(StationAndConnection phase : routePhases) {
            timeElapsed += phase.connection.getPureMinutesTimeCost(
                    Utils.calcMinuteOfDay(forStartingMinuteOfDay, timeElapsed)
            );
            interchangesCount += Utils.wasInterchange(currentLine, phase.connection.getLineLabel());
            currentLine = phase.connection.getLineLabel();
        }

        return new MacroParametersSummary(
                timeElapsed,
                interchangesCount,
                Measures.cost(timeElapsed,interchangesCount,interchangesCritter)
        );
    }

    public String sumUpMacroParameters(boolean interchangesCritter) {
        StringBuilder builder = new StringBuilder();

        MacroParametersSummary summary = calculateMacroParameters(interchangesCritter);

        builder.append(String.format("Time cost (minutes): %s%n", (int) summary.minutesElapsed));
        builder.append(String.format("Interchanges count: %s%n", summary.interchangesCount));
        builder.append(String.format("Total cost value: %s%n", summary.totalCost));
        builder.append('\n');

        return builder.toString();
    }


    public static ConstructedPath constructPath(Vertex dest, double startMinuteOfDay) {
        ConstructedPath result = new ConstructedPath(dest, startMinuteOfDay);

        List<ConstructedPath.StationAndConnection> phases = new ArrayList<>();

        Vertex current = dest;
        while (current.predecessor != null) {
            phases.add(
                    0,
                    new ConstructedPath.StationAndConnection(
                            current.predecessor,
                            current.linkToPredecessor
                    )
            );

            current = current.predecessor;
        }
        result.setRoutePhases(phases);

        return result;
    }

}
