package application;

import application.modules.Departure;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import message.parser.JSONParser;
import message.urlreader.URLReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Controller {
    public static final boolean __DEBUG = false;
    private static final int FIRSTSTOP = 1, SECONDSTOP = 2, THIRDSTOP = 3, FOURTHSTOP = 4, UPDATEINTERVALL = 2000, TIMEUPDATEINTERVALL = 250;
    private static final Color TEXTCOLOR = Color.rgb(0, 255, 0);
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Date[] lastupdate = new Date[4];
    private final Properties properties = getProperties();
    private final Font font = Font.loadFont(getClass().getResource("/font/VRRR.ttf").toString(), 50);
    private final Text timeText = new Text(),
            stop1LineText = new Text(), stop1StopText = new Text(), stop1TimeText = new Text(),
            stop2LineText = new Text(), stop2StopText = new Text(), stop2TimeText = new Text(),
            stop3LineText = new Text(), stop3StopText = new Text(), stop3TimeText = new Text(),
            stop4LineText = new Text(), stop4StopText = new Text(), stop4TimeText = new Text(),
            errorText = new Text();

    private final Text[] tarray = {
            stop1LineText, stop1StopText, stop1TimeText,
            stop2LineText, stop2StopText, stop2TimeText,
            stop3LineText, stop3StopText, stop3TimeText,
            stop4LineText, stop4StopText, stop4TimeText,
            timeText, errorText};
    @FXML
    TextFlow textFlowStop1, textFlowStop2, textFlowStop3, textFlowStop4;
    @FXML
    TextFlow textFlowLine1, textFlowLine2, textFlowLine3, textFlowLine4;
    @FXML
    TextFlow textFlowTime1, textFlowTime2, textFlowTime3, textFlowTime4;
    @FXML
    TextFlow errorFlow;
    @FXML
    TextFlow dateFlow;
    @FXML
    Text headingStop1, headingStop2, headingStop3, headingStop4;

    @FXML
    public void initialize() throws IOException {
        URLReader.initURLReader();
        setUpHeadlines();
        setUpTexts();
        setUpThread();
    }

    private void setUpHeadlines() {
        headingStop1.setText(properties.getProperty("firstStop_Name") + ":");
        headingStop1.setFont(font);
        headingStop2.setText(properties.getProperty("secondStop_Name") + ":");
        headingStop2.setFont(font);
        headingStop3.setText(properties.getProperty("thirdStop_Name") + ":");
        headingStop3.setFont(font);
        headingStop4.setText(properties.getProperty("fourthStop_Name") + ":");
        headingStop4.setFont(font);
    }

    private void setUpTexts() {

        for (Text t : tarray) {
            t.setFont(font);
            t.setFill(TEXTCOLOR);
        }
        errorText.setFill(Color.RED);
        dateFlow.getChildren().add(timeText);
        errorFlow.getChildren().add(errorText);
        textFlowLine1.getChildren().addAll(stop1LineText);
        textFlowStop1.getChildren().addAll(stop1StopText);
        textFlowTime1.getChildren().addAll(stop1TimeText);
        textFlowLine2.getChildren().addAll(stop2LineText);
        textFlowStop2.getChildren().addAll(stop2StopText);
        textFlowTime2.getChildren().addAll(stop2TimeText);
        textFlowLine3.getChildren().addAll(stop3LineText);
        textFlowStop3.getChildren().addAll(stop3StopText);
        textFlowTime3.getChildren().addAll(stop3TimeText);
        textFlowLine4.getChildren().addAll(stop4LineText);
        textFlowStop4.getChildren().addAll(stop4StopText);
        textFlowTime4.getChildren().addAll(stop4TimeText);
    }

    public void updateTime() {
        Date date = new Date();
        Platform.runLater(() -> timeText.setText(DATEFORMAT.format(date)));
    }

    private void setUpThread() {
        screenService.start();
        timeService.start();
    }

    private void loop() {
        setUpTextOnTextFlow(stop1LineText, stop1StopText, stop1TimeText, FIRSTSTOP);
        setUpTextOnTextFlow(stop2LineText, stop2StopText, stop2TimeText, SECONDSTOP);
        setUpTextOnTextFlow(stop3LineText, stop3StopText, stop3TimeText, THIRDSTOP);
        setUpTextOnTextFlow(stop4LineText, stop4StopText, stop4TimeText, FOURTHSTOP);
    }

    public void setUpTextOnTextFlow(Text line, Text flow, Text time, int stop) {
        boolean gotText = false;
        String stopURL;
        switch (stop) {
            case FIRSTSTOP:
                stopURL = "firstStop_URL";
                break;
            case SECONDSTOP:
                stopURL = "secondStop_URL";
                break;
            case THIRDSTOP:
                stopURL = "thirdStop_URL";
                break;
            case FOURTHSTOP:
                stopURL = "fourthStop_URL";
                break;
            default:
                stopURL = "";
                break;
        }
        String[] text = new String[3];
        try {
            text = getNewText(stopURL, stop);
            if (text[2].contains("min")) {
                gotText = true;
            }
        } catch (Exception ignored) {
        }
        if (!gotText) {
            text = calculateTimeAfterError(line, flow, time, stop);
        }


        final String[] finalText = text;
        Platform.runLater(() -> {
            line.setText(finalText[0]);
            flow.setText(finalText[1]);
            time.setText(finalText[2]);

        });
    }

    private String[] getNewText(String property, int screenStop) throws Exception {
        String line = "";
        String stop = "";
        String time = "";

        List<Departure> departures = new JSONParser().getDepartureListFromURL(new URL(properties.getProperty(property)));

        for (Departure departure : departures) {
            line += trimString(departure.getLineName(), 4) + "\n";
            stop += trimString(departure.getDestination(), 18) + "\n";
            time += trimString(departure.getDepartureTime(), 6) + "\n";
        }

        String[] text = new String[3];
        text[0] = line;
        text[1] = stop;
        text[2] = time;


        if (__DEBUG) {
            System.out.println(text[0]);
            System.out.println(text[1]);
            System.out.println(text[2]);
        }

        lastupdate[screenStop - 1] = new Date();
        Platform.runLater(() -> errorText.setText(""));
        return text;
    }

    private String trimString(String string, int maxSize) {
        return string.substring(0, Math.min(string.length(), maxSize));
    }

    private String[] calculateTimeAfterError(Text line, Text flow, Text time, int stop) {
        String[] text = new String[3];
        Platform.runLater(() -> errorText.setText("Connection Error"));

        long ellapsedTime = (long) ((new Date().getTime() - lastupdate[stop - 1].getTime()) / 60000.);
        if (ellapsedTime > 0) {
            lastupdate[stop - 1] = new Date();
        }
        String[] minutesToWait = time.getText().split("\n");
        String[] newTime = new String[minutesToWait.length];
        for (int i = 0; i < minutesToWait.length; i++) {
            String t = minutesToWait[i];
            if (t.contains("min")) {
                long newMinutes = (Integer.parseInt(t.split(" ")[0]) - ellapsedTime);
                newTime[i] = newMinutes > 0 ? String.valueOf(newMinutes) : "";
            } else {
                newTime[i] = "";
            }
        }
        String newText[] = new String[minutesToWait.length];
        for (int i = 0; i < newTime.length; i++) {
            String t = newTime[i];
            if (t != "")
                newText[i] = t + " min";
            else
                newText[i] = "Sofort";
        }
        text[0] = line.getText();
        text[1] = flow.getText();
        text[2] = String.join("\n", newText);

        return text;

    }

    private Properties getProperties() {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(this.getClass().getResourceAsStream
                ("/properties/properties.prop"));
        try {
            properties.load(new InputStreamReader(stream, Charset.forName("UTF-8")));
            stream.close();
        } catch (Exception e) {
            if (__DEBUG) e.printStackTrace();
        }
        return properties;
    }

    Service<Void> screenService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        while (true) {
                            loop();
                            Thread.sleep(UPDATEINTERVALL);
                        }
                    } catch (Exception e) {
                        if (__DEBUG) e.printStackTrace();
                        Platform.runLater(() -> screenService.restart());
                    }
                    return null;
                }

            };
        }
    };


    Service<Void> timeService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        while (true) {
                            updateTime();
                            Thread.sleep(TIMEUPDATEINTERVALL);
                        }
                    } catch (Exception e) {
                        if (__DEBUG) e.printStackTrace();
                        Platform.runLater(() -> timeService.restart());
                    }
                    return null;
                }

            };
        }
    };


}
