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
    String name;
    URL url;
    List<Departure> departures;
    Font font;
    Color textcolor;
    Text lineNameColumn;
    Text destinationColumn;
    Text departureTimeColumn;

    public Station(Font font, Color textcolor, String name, URL url) {
        this.font = font;
        this.textcolor = textcolor;
        this.name = name;
        this.url = url;
    }

    public void generateLineColumn() {
        StringBuilder lineNameColumn = new StringBuilder();
        for (Departure departure : departures) {
            lineNameColumn.append(trimString(departure.lineName, 4)).append("\n");
        }
        this.lineNameColumn.setText(lineNameColumn.toString());
    }

    public void generateDestinationColumn() {
        StringBuilder destinationColumn = new StringBuilder();
        for (Departure departure : departures) {
            destinationColumn.append(trimString(optimizeDestinationNames(departure.destination), 17)).append("\n");
        }
        this.destinationColumn.setText(destinationColumn.toString());
    }

    public void generateDepartureTimeColumn() {
        StringBuilder departureTimeColumn = new StringBuilder();
        for (Departure departure : departures) {
            if (departure.departureTime > 0) {
                departureTimeColumn.append(departure.departureTime).append(" ").append("min").append("\n");
            } else {
                departureTimeColumn.append("\n");
            }
        }
        this.departureTimeColumn.setText(departureTimeColumn.toString());
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
                .toList());
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
