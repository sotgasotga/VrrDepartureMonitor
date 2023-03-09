package application.modules;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Departure {
    String lineName;
    String destination;
    int originalDepartureTime;
    int departureTime;

    public Departure(String lineName, String destination, int originalDepartureTime) {
        this.lineName = lineName;
        this.destination = destination;
        this.originalDepartureTime = originalDepartureTime;
        this.departureTime = originalDepartureTime;
    }
}