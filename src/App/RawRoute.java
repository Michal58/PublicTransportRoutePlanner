package App;

import java.time.LocalTime;

public record RawRoute(
        String lineLabel,
        LocalTime departureTime,
        LocalTime arrivalTime,
        String startLabel,
        String endLabel,
        Coordinates startCoordinates,
        Coordinates endCoordinates
)
{ }
