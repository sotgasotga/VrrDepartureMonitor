package application.modules;

import application.JSONParser;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

@Data
@Builder
public class Station {
    int hopefullyUselessID;
    String name;
    URL url;
    List<Departure> departures;
    Font font;
    Color textcolor;
    Text lineNameColumn;
    Text destinationColumn;
    Text departureTimeColumn;

    public void generateLineColumn() {
        String lineNameColumn = "";
        for (Departure departure : departures) {
            lineNameColumn += trimString(departure.lineName, 4) + "\n";
        }
        this.lineNameColumn.setText(lineNameColumn);
    }

    public void generateDestinationColumn() {
        String destinationColumn = "";
        for (Departure departure : departures) {
            destinationColumn += trimString(optimizeDestinationNames(departure.destination), 17) + "\n";
        }
        this.destinationColumn.setText(destinationColumn);
    }

    public void generateDepartureTimeColumn() {
        String departureTimeColumn = "";
        for (Departure departure : departures) {
            if (departure.departureTime > 0) {
                departureTimeColumn += departure.departureTime + " " + "min" + "\n";
            } else {
                departureTimeColumn += "\n";
            }
        }
        this.departureTimeColumn.setText(departureTimeColumn);
    }

    public void setupTexts(TextFlow lineNameColumnTextFlow, TextFlow destinationColumnTextFlow, TextFlow departureTimeColumnTextFlow, Text heading) {
        initTexts();
        setFontAndColor();
        heading.setText(name);
        heading.setFont(font);
        lineNameColumnTextFlow.getChildren().addAll(lineNameColumn);
        destinationColumnTextFlow.getChildren().addAll(destinationColumn);
        departureTimeColumnTextFlow.getChildren().addAll(departureTimeColumn);
    }

    private void initTexts() {
        lineNameColumn = new Text();
        destinationColumn = new Text();
        departureTimeColumn = new Text();
    }

    private void setFontAndColor() {
        lineNameColumn.setFont(font);
        destinationColumn.setFont(font);
        departureTimeColumn.setFont(font);

        lineNameColumn.setFill(textcolor);
        destinationColumn.setFill(textcolor);
        departureTimeColumn.setFill(textcolor);
    }

    public void updateDepartures(){
        generateLineColumn();
        generateDestinationColumn();
        generateDepartureTimeColumn();
    }

    private void removeDeparted() {
        departures.removeAll(departures.stream()
                .filter(i -> i.departureTime <= 0)
                .collect(Collectors
                    .toList()));
    }

    public void updateDeparturesOffline(int timeSinceLastDownload) {
        generateDeparturesIfNull();
        for (Departure departure : departures) {
            departure.setDepartureTime(departure.getOriginalDepartureTime() - timeSinceLastDownload);
        }
        removeDeparted();
        generateLineColumn();
        generateDestinationColumn();
        generateDepartureTimeColumn();
    }

    private void generateDeparturesIfNull() {
        if (departures == null) {
            this.departures = new LinkedList<>();
        }
    }

    public void downloadDepartures() throws IOException {
        departures = new JSONParser().getDepartureListFromURL(url);
    }

    private String trimString(String string, int maxSize) {
        return string.substring(0, Math.min(string.length(), maxSize));
    }

    /**
     * Remove Prefixes Like "D-".
     * Other Example "Köln Flughafen Köln/" -> "Köln Flughafen"
     * @param destinationName input String
     * @return output String
     */
    private String optimizeDestinationNames(String destinationName) {
        destinationName = destinationName.replace("D-","");
        destinationName = destinationName.replace(" Köln/", " ");
        destinationName = destinationName.replace("Hauptbahnhof", "Hbf");
        destinationName = destinationName.replace(", ", " ");
        return destinationName;
    }
}
