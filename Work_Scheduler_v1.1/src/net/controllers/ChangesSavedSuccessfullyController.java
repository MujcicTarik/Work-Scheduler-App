package net.controllers;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ChangesSavedSuccessfullyController {

    @FXML
    private ImageView exitButton;

    @FXML
    void handleExitButton(MouseEvent event) {
        ((Stage) exitButton.getScene().getWindow()).close();
    }

    //Opacity hover change
    public void handleHoverButtonEnter() {
        double hover = 0.7;
        if (exitButton.isHover())
            exitButton.setOpacity(hover);
    }

    public void handleHoverButtonExit() {
        if (!exitButton.isHover())
            exitButton.setOpacity(1);
    }

}