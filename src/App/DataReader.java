package App;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DataReader {
    private String filenameToRead;

    public DataReader(String filenameToRead) {
        this.filenameToRead = filenameToRead;
    }
    public LocalTime hourToTime(String sTimeForm) {
        String[] times = sTimeForm.split(":");

        int hour = Integer.parseInt(times[0]);
        int minute = Integer.parseInt(times[1]);

        return LocalTime.of(
                hour < 24 ? hour : hour - 24,
                minute
        );
    }
    public List<RawRoute> getRoutes() {
        List<RawRoute> routes;

        try (CSVReader reader = new CSVReader(new FileReader(this.filenameToRead))) {
            List<String[]> rows = reader.readAll();
            rows.remove(0);
            routes = new ArrayList<>(rows.size());

            for (String[] row : rows) {
                RawRoute route = new RawRoute(
                        row[0],
                        hourToTime(row[1]),
                        hourToTime(row[2]),
                        row[3],
                        row[4],
                        new Coordinates(
                                Double.parseDouble(row[5]),
                                Double.parseDouble(row[6])
                        ),
                        new Coordinates(
                                Double.parseDouble(row[7]),
                                Double.parseDouble(row[8])
                        )
                );
                routes.add(route);
            }

        } catch(IOException | CsvException e){
            throw new RuntimeException(e);
        }

        return routes;
    }
}
