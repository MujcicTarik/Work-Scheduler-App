package net.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ErrorScheduleAlreadyExistsController implements Initializable {

    @FXML
    private ImageView errorExitButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Tooltip.install(errorExitButton, new Tooltip("Close window")); //gives a tooltip on the exit button
    }


    public void handleExitButton() throws IOException {
        Stage stage = (Stage) errorExitButton.getScene().getWindow();
        stage.close();
    }

    //Opacity hover change
    public void handleHoverButtonEnter() {
        if(errorExitButton.isHover())
            errorExitButton.setOpacity(0.7);
    }
    public void handleHoverButtonExit() {
        if(!errorExitButton.isHover())
            errorExitButton.setOpacity(1);
    }


}
