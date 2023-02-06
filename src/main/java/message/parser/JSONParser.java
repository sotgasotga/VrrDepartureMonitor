package message.parser;


import application.modules.Departure;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

public class JSONParser {

    public List<Departure> getDepartureListFromURL(URL url) throws IOException {
        List<Departure> departures = new LinkedList<>();
        for (JsonNode jsonNode : new ObjectMapper().readTree(url).get("preformatted")) {
            departures.add(new Departure(
                    removeQuotes(jsonNode.get(0).toString()),
                    removeQuotes(jsonNode.get(1).toString()),
                    removeQuotes(jsonNode.get(2).toString())));
        }
        return departures;
    }

    private String removeQuotes(String input) {
        return input.replace("\"", "");
    }
}
