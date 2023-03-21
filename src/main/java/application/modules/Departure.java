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

    /**
     * Diese Methode sollte durch Lomboks @Data automatisch generiert werden,
     * das scheint aber nicht zu passieren
     * @return originalDepartureTime
     */
    public int getOriginalDepartureTime() {
        return originalDepartureTime;
    }

    /**
     * Diese Methode sollte durch Lomboks @Data automatisch generiert werden,
     * das scheint aber nicht zu passieren
     */
    public void setDepartureTime(int departureTime) {
        this.departureTime = departureTime;
    }
}