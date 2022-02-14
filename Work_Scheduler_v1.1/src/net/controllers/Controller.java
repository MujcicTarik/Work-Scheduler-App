package net.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class Controller implements Initializable {

    @FXML
    private ImageView exitButton; //variable which we use to reference to the exit button added using the scene builder (it has fx:id = "exitButton"

    @FXML
    private StackPane contentArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        /*title.setTranslateX(-title.getLayoutX() + Screen.getPrimary().getVisualBounds().getWidth() - 1000);*/
        /*exitButton.setTranslateX(-exitButton.getLayoutX() + Screen.getPrimary().getVisualBounds().getWidth()-44);*/ //code for placing the exit button in top right
        //exitButton.setOnMouseClicked(event -> System.exit(0));


        try {
            Parent fxml = FXMLLoader.load(getClass().getResource("../" + "fxml" + File.separator + "newSchedule.fxml"));
            contentArea.getChildren().removeAll();    //to clear the content area
            contentArea.getChildren().setAll(fxml);   //to add content to the contentArea from fxml variable (which here has newSchedule.fxml)

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void newSchedule(javafx.event.ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("../" + "fxml" + File.separator + "newSchedule.fxml"));
        contentArea.getChildren().removeAll(); //to clear the content area
        contentArea.getChildren().setAll(fxml);//to add content to the contentArea from fxml variable (which here has newSchedule.fxml)
    }

    public void editSchedule(javafx.event.ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("../" + "fxml" + File.separator + "editSchedule.fxml"));
        contentArea.getChildren().removeAll(); //to clear the content area
        contentArea.getChildren().setAll(fxml);//to add content to the contentArea from fxml variable (which here has editSchedule.fxml)
    }

    public void generateSchedule(javafx.event.ActionEvent actionEvent) throws IOException {
        Parent fxml = FXMLLoader.load(getClass().getResource("../" + "generateschedule" + File.separator + "fxml" + File.separator + "generateSchedule.fxml"));
        contentArea.getChildren().removeAll(); //to clear the content area
        contentArea.getChildren().setAll(fxml);//to add content to the contentArea from fxml variable (which here has generateSchedule.fxml)
    }


    public void handleExitButton () throws IOException {
        Stage newWindow = new Stage();
        newWindow.setTitle("Are you sure you want to leave?");
        newWindow.initModality(Modality.APPLICATION_MODAL);
        newWindow.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/confirmExit.fxml"));
        newWindow.setScene(new Scene(loader.load()));
        newWindow.show();
    }

    //Opacity hover change
    public void handleHoverButtonEnter() {
        if(exitButton.isHover())
            exitButton.setOpacity(0.7);
    }
    public void handleHoverButtonExit() {
        if(!exitButton.isHover())
            exitButton.setOpacity(1);
    }




}
