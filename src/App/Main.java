package App;

import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
        DataReader reader = new DataReader(args[0]);

        Graph g = new Graph(reader.getRoutes().stream().toList());
        ResultsGetter rg = new ResultsGetter();

        Object[][] parametersToCheck = new Object[][] {
                new Object[] {g, "Zajezdnia Obornicka","Paprotna", LocalTime.of(20, 50)},
                new Object[] {g, "Krucza","Kleczkowska",LocalTime.of(13,15)},
                new Object[] {g, "Rogowska (ogrody działkowe)","Lipa Piotrowska",LocalTime.of(23,45)},
                new Object[] {g, "Pola","KROMERA",LocalTime.of(1,0)},
                new Object[] {g, "DWORZEC GŁÓWNY","PL. GRUNWALDZKI",LocalTime.of(9,0)},
                new Object[] {g, "Spółdzielcza","Chwałkowska",LocalTime.of(9,0)}
        };


        rg.prepareResultsForAStarAlgorithmWithPureTime(parametersToCheck);
//        rg.prepareResultsForAStartAlgorithmWithInterchanges(parametersToCheck);


        // can be inserted
//        new Object[] {
//                g,
//                "Rogowska (ogrody działkowe)",
//                List.of(
//                        "Lipa Piotrowska",
//                        "DWORZEC GŁÓWNY",
//                        "KIEŁCZOWSKA (LZN)",
//                        "PL. GRUNWALDZKI",
//                        "LEŚNICA",
//                        "Magellana",
//                        "PL. JANA PAWŁA II",
//                        "Szkolna",
//                        "Kleczkowska",
//                        "MIŃSKA (Rondo Rotm. Pileckiego)"
//                ),
//                LocalTime.of(10,25)
//        }


//        parametersToCheck = new Object[][] {
//                new Object[] {
//                        g,
//                        "Czajkowskiego",
//                        List.of(
//                                "Babimojska",
//                                "ROD Zgoda",
//                                "Stalowa",
//                                "Maślice Małe (Brodnicka)",
//                                "Bystrzycka",
//                                "Końcowa",
//                                "Budziszyńska",
//                                "Rędzińska (Cmentarz)",
//                                "Pomorska",
//                                "Muchobór Wielki",
//                                "Ćwiczebna"
//                        ),
//                        LocalTime.of(20,50)
//                }
//        };

//        rg.prepareResultsForTravellingSalesmanWithPureTime(parametersToCheck);
//        rg.prepareResultsForTravellingSalesmanWithInterchanges(parametersToCheck);
    }
}