package App;

import java.util.List;

import static Utils.Utils.EARTH_RADIUS;

public class Measures {
    public static final double INTERCHANGE_COST = 24 * 1000000;

    public static double cost(double timeElapsed, int interchangeCount, boolean interchangeCritter) {
        return timeElapsed + (interchangeCritter ? interchangeCount * INTERCHANGE_COST : 0);
    }

    public static double haversine(double firstLat, double firstLon, double secondLat, double secondLon) {
        double radFirstLat = Math.toRadians(firstLat);
        double radSecondLat = Math.toRadians(secondLat);

        double dLat = radSecondLat - radFirstLat;
        double dLon = Math.toRadians(secondLon - firstLon);

        double a = Math.pow(Math.sin(dLat/2), 2)
                + Math.pow(Math.sin(dLon/2), 2)
                * Math.cos(radFirstLat)
                * Math.cos(radSecondLat);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return EARTH_RADIUS * c;
    }

    public static class PathMeasures {
        public List<Vertex> path;
        public double cost;
        public double timeElapsed;
        public int interchangesCount;

        public PathMeasures(List<Vertex> path, double cost, double timeElapsed, int interchangesCount) {
            this.path = path;
            this.cost = cost;
            this.timeElapsed = timeElapsed;
            this.interchangesCount = interchangesCount;
        }
    }

    public static class EdgeMeasures {
        public Edge edge;
        public double cost;
        public double timeElapsed;
        public int interchangesCount;
    }
}
