package application.modules;

import interfaces.Updateable;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by Timo on 22.04.17.
 */
public class Date extends Label implements Updateable {


    public Date() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy  hh:mm a");
        this.setText(LocalDateTime.now().format(formatter));
    }

    @Override
    public void update() {

    }
}
