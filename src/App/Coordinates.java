package App;

public record Coordinates(double latitude, double longitude) {
    public Coordinates copy() {
        return new Coordinates(latitude, longitude);
    }
}
