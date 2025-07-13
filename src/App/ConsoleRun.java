package App;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class ConsoleRun {
    // parameters: <srcStation> <destStation> <criterion> <localtime> <csv_routes>
    // example: Spółdzielcza "Chwałkowska" t 09:00 data/refactoerd_graph.csv

    // OR

    // parameters: <srcStation> <stationsToMeet> <criterion> <localtime> <csv_routes>
    // example: Spółdzielcza "Chwałkowska";"Rogowska (ogrody działkowe)" t 09:00 data/refactoerd_graph.csv

    public static String cleanArg(String arg) {
        return arg.trim().replaceAll("\"","");
    }

    public static List<String> getListToInclude(String listArg) {
        return Arrays.stream(
                    listArg.trim().split(";")
                )
                .map(ConsoleRun::cleanArg)
                .toList();
    }
    public static void main(String[] args) {
        DataReader reader = new DataReader(args[4]);

        Graph g = new Graph(reader.getRoutes().stream().toList());
        ResultsGetter rg = new ResultsGetter();

        if (!args[2].matches("[tp]"))
            throw new RuntimeException("Invalid optimization critter");
        boolean interchangesCritter = args[2].equals("p");

        String src = cleanArg(args[0]);
        String dest = cleanArg(args[1]);
        LocalTime startTime = LocalTime.parse(args[3]);

        boolean isDestList = dest.contains(";");

        Object[][] parametersToCheck;
        if (!isDestList) {
            parametersToCheck = new Object[][]{
                    new Object[]{g, src, dest, startTime}
            };
        } else {
            parametersToCheck = new Object[][] {
                    new Object[] {
                            g,
                            src,
                            getListToInclude(args[1]),
                            startTime
                    }
            };
        }

        if (!isDestList) {
            if (!interchangesCritter)
                rg.prepareResultsForAStarAlgorithmWithPureTime(parametersToCheck);
            else
                rg.prepareResultsForAStartAlgorithmWithInterchanges(parametersToCheck);
        }
        else {
            if (!interchangesCritter)
                rg.prepareResultsForTravellingSalesmanWithPureTime(parametersToCheck);
            else
                rg.prepareResultsForTravellingSalesmanWithInterchanges(parametersToCheck);
        }
    }
}
