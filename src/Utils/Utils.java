package Utils;

import java.time.LocalTime;
import java.util.List;
import java.util.Random;

public class Utils {
    public static final double INF=Double.MAX_VALUE;
    public static final double EARTH_RADIUS = 6371.0;

    public final static int MINUTES_IN_HOUR = 60;
    public final static int FULL_DAY_MINUTES = MINUTES_IN_HOUR * 24;

    public static double latitudeToKm(double latValue){
        return (latValue * Math.PI / 180) * EARTH_RADIUS;
    }

    public static double longitudeToKm(double lonValue, double latValue){
        return (lonValue * Math.PI / 180) * EARTH_RADIUS * Math.cos(Math.toRadians(latValue));
    }

    public static double normMinute(double anyMinuteOfDay){
        return anyMinuteOfDay % FULL_DAY_MINUTES;
    }

    public static double secondOfDay(double minuteOfDay){
        return minuteOfDay * 60;
    }

    public static double localTimeToMinuteOfDay(LocalTime local){
        return (double) local.toSecondOfDay() / 60;
    }

    public static LocalTime timeOfDayFrom(double minuteOfDay) {
        return LocalTime.ofSecondOfDay((int) secondOfDay(minuteOfDay));
    }
    public static double calcMinuteOfDay(double startMinute, double minutesElapsed) {
        return normMinute(startMinute + minutesElapsed);
    }

    public static int wasInterchange(String currentLine, String previousLine) {
        return currentLine == null || currentLine.equals(previousLine) ? 0 : 1;
    }

    public static class RefMedium<T>{
        public T v;
        public RefMedium(T v){
            this.v = v;
        }
    }

    public static <T>  T getLast(List<T> someList) {
        return someList.get(someList.size() - 1);
    }

    public static <T> void swap(T[] arr, int i, int j) {
        T aux = arr[i];
        arr[i] = arr[j];
        arr[j] = aux;
    }

    //30370
    //3796
    //3793

    public static int SEED = 3793;
    public static Random randomizer = new Random(SEED);
}
