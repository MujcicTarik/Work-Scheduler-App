package net.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class ErrorNoShiftsCreatedController {
    @FXML
    private ImageView errorExitButton; //references on the exit ImageView

    public void handleExitButton () throws IOException {
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
