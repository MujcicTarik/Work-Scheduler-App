package net.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class ErrorShiftAlreadyExistsController {
    @FXML
    ImageView errorExitButton;

    public void handleExitButton() {
        ((Stage) errorExitButton.getScene().getWindow()).close();
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
