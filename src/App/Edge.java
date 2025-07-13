package App;

import java.time.LocalTime;

import static Utils.Utils.FULL_DAY_MINUTES;
import static Utils.Utils.MINUTES_IN_HOUR;

public class Edge {
    public final double INTERCHANGE_COST = 24 * 1000000;

    private String lineLabel;
    private int departureMinuteOfDay;
    private int minutesTransportCost;



    public Edge(RawRoute source) {
        this.lineLabel = source.lineLabel();
        this.departureMinuteOfDay = source.departureTime().toSecondOfDay() / MINUTES_IN_HOUR;
        this.minutesTransportCost = ((source.arrivalTime().toSecondOfDay() / MINUTES_IN_HOUR) - this.departureMinuteOfDay + FULL_DAY_MINUTES) % FULL_DAY_MINUTES;
    }


    private Edge(String lineLabel) {
        this.lineLabel=lineLabel;
    }
    private Edge(String label, int departureMinuteOfDay, int minutesTransportCost) {
        this.lineLabel = label;
        this.departureMinuteOfDay = departureMinuteOfDay;
        this.minutesTransportCost = minutesTransportCost;
    }

    public static Edge createLabelledEdge(String label) {
        return new Edge(label);
    }
    public static Edge createCustomEdge(String label, int departureMinuteOfDay, int minutesTransportCost) {
        return new Edge(label,departureMinuteOfDay,minutesTransportCost);
    }

    public double getPureMinutesTimeCost(double minuteTimeOfDayAtThisStationArrival) {
        double waitTime = departureMinuteOfDay - minuteTimeOfDayAtThisStationArrival;
        if (waitTime < 0)
            waitTime += FULL_DAY_MINUTES;
        return waitTime + minutesTransportCost;
    }

    public double getPureInterchangeCost(String thisStationArrivalLineLabel) {
        return thisStationArrivalLineLabel == null || thisStationArrivalLineLabel.equals(this.lineLabel)? 0 : INTERCHANGE_COST;
    }

    public double getInterchangeAndTimeCost(String stationArrivalLineLabel, double minuteTimeOfDayAtThisStationArrival) {
        return getPureInterchangeCost(stationArrivalLineLabel) + getPureMinutesTimeCost(minuteTimeOfDayAtThisStationArrival);
    }

    public double getNextStationArrivalTime() {
        return (this.departureMinuteOfDay + this.minutesTransportCost) % FULL_DAY_MINUTES;
    }

    public void setNextStationArrivalTime(int nextStationArrivalTime) {
        int newNextStationArrivalTime = (nextStationArrivalTime + FULL_DAY_MINUTES) % FULL_DAY_MINUTES;
        this.minutesTransportCost = (newNextStationArrivalTime - departureMinuteOfDay + FULL_DAY_MINUTES) % FULL_DAY_MINUTES;
    }

    public String getStrNextStationArrivalTime() {
        return LocalTime.ofSecondOfDay((long) (getNextStationArrivalTime()*60)).toString();
    }

    public String getLineLabel() {
        return lineLabel;
    }

    public int getDepartureMinuteOfDay() {
        return departureMinuteOfDay;
    }

    public String getStrDepartureMinuteOfDay() {
        return LocalTime.ofSecondOfDay(departureMinuteOfDay* 60L).toString();
    }

    @Override
    public String toString() {
        return String.format("%5s%7s%7s",lineLabel, LocalTime.ofSecondOfDay(departureMinuteOfDay* 60L),LocalTime.ofSecondOfDay((int) getNextStationArrivalTime()*60L));
    }
}
