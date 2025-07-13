package App;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;

public class EdgesToSameVertex extends HashSet<Edge> {
    public EdgesToSameVertex() {
        super();
    }

    public Optional<Edge> getTheBestEdgeWithPureTimeCriteria(double minuteTimeOfDay) {
        return this.stream()
                .min(
                        Comparator.comparingDouble(o ->
                                o.getPureMinutesTimeCost(minuteTimeOfDay)
                        )
                );
    }


    public Optional<Edge> getTheBestEdgeWithInterchangeAndTimeCriteria(String arrivalLine, double minuteTimeOfDay) {
        return this.stream()
                .min(
                        Comparator.comparingDouble(o ->
                                o.getInterchangeAndTimeCost(arrivalLine, minuteTimeOfDay)
                        )
                );
    }
}
