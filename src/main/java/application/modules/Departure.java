package application.modules;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class Departure {
    String lineName;
    String destination;
    String departureTime;

    public Departure(String lineName, String destination, String departureTime) {
        this.lineName = lineName;
        this.destination = destination;
        this.departureTime = departureTime;
    }
}