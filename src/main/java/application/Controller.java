package application;

import application.modules.Station;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

public class Controller {
    public static final boolean __DEBUG = false;
    private static final int UPDATEINTERVALL = 2000, TIMEUPDATEINTERVALL = 250;
    private static final Color TEXTCOLOR = Color.rgb(0, 255, 0);
    private static final DateFormat DATEFORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static LocalTime lastDownloadTime = LocalTime.now();
    private final Properties properties = getProperties();
    private final Font font = Font.loadFont(Objects.<URL>requireNonNull(getClass().getResource("/font/VRRR.ttf")).toString(), 50);
    private Station station1, station2, station3, station4;
    private final Text timeText = new Text(), errorText = new Text();

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
        generateStations();
        setUpTexts();
        setUpThread();
    }

    private void setUpTexts() {
        station1.setupTexts(textFlowLine1, textFlowStop1, textFlowTime1, headingStop1);
        station2.setupTexts(textFlowLine2, textFlowStop2, textFlowTime2, headingStop2);
        station3.setupTexts(textFlowLine3, textFlowStop3, textFlowTime3, headingStop3);
        station4.setupTexts(textFlowLine4, textFlowStop4, textFlowTime4, headingStop4);

        timeText.setFont(font);
        timeText.setFill(TEXTCOLOR);
        errorText.setFont(font);
        errorText.setFill(Color.RED);
        dateFlow.getChildren().add(timeText);
        errorFlow.getChildren().add(errorText);
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
        try {
            downloadDepartures();
            updateDepartures();
            Platform.runLater(() -> errorText.setText(""));
        } catch (IOException e) {
            Platform.runLater(() -> errorText.setText("Connection Error"));
            updateDeparturesOffline();
        }
    }

    private void updateDepartures() {
        station1.updateDepartures();
        station2.updateDepartures();
        station3.updateDepartures();
        station4.updateDepartures();
    }

    private void updateDeparturesOffline() {
        int time = timeSinceLastDownload();
        station1.updateDeparturesOffline(time);
        station2.updateDeparturesOffline(time);
        station3.updateDeparturesOffline(time);
        station4.updateDeparturesOffline(time);
    }

    private void downloadDepartures() throws IOException {
        station1.downloadDepartures();
        station2.downloadDepartures();
        station3.downloadDepartures();
        station4.downloadDepartures();
        lastDownloadTime = LocalTime.now();
    }

    private int timeSinceLastDownload() {
        return (int) Math.abs(Duration.between(LocalTime.now(), lastDownloadTime).toMinutes());
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        BufferedInputStream stream = new BufferedInputStream(Objects.requireNonNull(this.getClass().getResourceAsStream("/properties/properties.prop")));
        try {
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
            stream.close();
        } catch (Exception e) {
            if (__DEBUG) e.printStackTrace();
        }
        return properties;
    }

    private void generateStations() throws MalformedURLException {
        station1 = new Station(font, TEXTCOLOR,
                properties.getProperty("firstStop_Name") + ":",
                new URL(properties.getProperty("firstStop_URL")));
        station2 = new Station(font, TEXTCOLOR,
                properties.getProperty("secondStop_Name") + ":",
                new URL(properties.getProperty("secondStop_URL")));
        station3 = new Station(font, TEXTCOLOR,
                properties.getProperty("thirdStop_Name") + ":",
                new URL(properties.getProperty("thirdStop_URL")));
        station4 = new Station(font, TEXTCOLOR,
                properties.getProperty("fourthStop_Name") + ":",
                new URL(properties.getProperty("fourthStop_URL")));
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
