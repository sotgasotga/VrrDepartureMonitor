package application;


import application.modules.Departure;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;

public class JSONParser {

    public List<Departure> getDepartureListFromURL(URL url) throws IOException {
        List<Departure> departures = new LinkedList<>();
        for (JsonNode jsonNode : getJSONNode(url)) {
            departures.add(new Departure(
                    removeQuotes(jsonNode.get(0).toString()),
                    removeQuotes(jsonNode.get(1).toString()),
                    timeToInt(removeQuotes(jsonNode.get(2).toString()))));
        }
        return departures;
    }

    private JsonNode getJSONNode(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(2000);
        connection.setReadTimeout(2000);
        connection.setUseCaches(false);
        connection.setDefaultUseCaches(false);
        connection.setRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        connection.setRequestProperty("Pragma", "no-cache");
        connection.setRequestProperty("Expires", "0");
        connection.connect();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String jsonInhalt = in.lines().reduce("", String::concat);
        in.close();
        return new ObjectMapper().readTree(jsonInhalt).get("preformatted");
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
