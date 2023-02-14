package application;


import application.modules.Departure;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class JSONParser {

    public List<Departure> getDepartureListFromURL(URL url) throws IOException {
        List<Departure> departures = new LinkedList<>();
        for (JsonNode jsonNode : new ObjectMapper().readTree(url).get("preformatted")) {
            departures.add(new Departure(
                    removeQuotes(jsonNode.get(0).toString()),
                    removeQuotes(jsonNode.get(1).toString()),
                    timeToInt(removeQuotes(jsonNode.get(2).toString()))));
        }
        return departures;
    }

    private String removeQuotes(String input) {
        return input.replace("\"", "");
    }

    private int timeToInt(String time) {
        String result = trimString(time, 2);
        if (result.isEmpty()) {
            return -1;
        } else {
            return Integer.parseInt(result.trim());
        }
    }

    private String trimString(String string, int maxSize) {
        return string.substring(0, Math.min(string.length(), maxSize));
    }
}
