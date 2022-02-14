package net.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.schedule.Schedule;
import net.schedule.Shift;

import java.io.File;
import java.io.IOException;
import java.lang.management.PlatformLoggingMXBean;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class NewScheduleController implements Initializable {

    @FXML
    private JFXListView shiftList;
    @FXML
    private JFXListView workersList;
    @FXML
    private JFXButton buttonNewShift;
    @FXML
    private JFXButton buttonNewWorker;
    @FXML
    private TextField scheduleName;
    @FXML
    private JFXButton buttonSave;


    private static Schedule temp = new Schedule();
    private boolean active = true;


    double x,y; //help variables for taking position on the window

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if(temp.getShifts() != null) {
            shiftList.setItems(FXCollections.observableList(temp.getShifts()));
        }
        if(temp.getWorkers() != null){
            workersList.setItems(FXCollections.observableList(temp.getWorkers()));
        }

        //workersList.setItems(FXCollections.observableList(Schedule.getWorkers()));
        //Schedule schedule1 = Schedule.readSchedule(naziv);



    }

    public void handleNewShift() throws IOException {

        Stage newWindow = new Stage();
        newWindow.setTitle("Add Shift");
        newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
        newWindow.initStyle(StageStyle.UNDECORATED);

//Create view from FXML
        Parent loader = FXMLLoader.load(getClass().getResource("../fxml/addNewShift.fxml"));

        loader.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        loader.setOnMouseDragged(event -> {
            newWindow.setX(event.getScreenX() - x);
            newWindow.setY(event.getScreenY() - y);
        });

        newWindow.setScene(new Scene(loader)); //Set view in window
        newWindow.show(); //Launch

    }

    public void handleNewWorker() throws IOException {
        if(shiftList.getItems().isEmpty()){
            Stage newWindow = new Stage();
            newWindow.setTitle("ERROR!");
            newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
            newWindow.initStyle(StageStyle.UNDECORATED);
            //Create view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorNoShiftsCreated.fxml"));
            //Set view in window
            newWindow.setScene(new Scene(loader.load()));
            //Launch
            newWindow.show();
        }
        else {
            Stage newWindow = new Stage();
            newWindow.setTitle("Add Worker");
            newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
            newWindow.initStyle(StageStyle.UNDECORATED);
            //Create view from FXML
            Parent loader = FXMLLoader.load(getClass().getResource("../fxml/addNewWorker.fxml"));

            loader.setOnMousePressed(event -> {
                x = event.getSceneX();
                y = event.getSceneY();
            });

            loader.setOnMouseDragged(event -> {
                newWindow.setX(event.getScreenX() - x);
                newWindow.setY(event.getScreenY() - y);
            });

            //Set view in window
            newWindow.setScene(new Scene(loader));
            //Launch
            newWindow.show();
        }

    }

    public void handleSave() throws IOException {

        if(scheduleName.getText().isEmpty()){  //to make sure that you can't create a schedule that has no name
            Stage newWindow = new Stage();
            newWindow.setTitle("ERROR!");
            newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
            newWindow.initStyle(StageStyle.UNDECORATED);
            //Create view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorNoScheduleName.fxml"));
            //Set view in window
            newWindow.setScene(new Scene(loader.load()));
            //Launch
            newWindow.show();
        }
        else{
            try {
                File file = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + scheduleName.getText());
                if (!file.exists()) {
                    file.mkdirs();
                    temp.setScheduleName(scheduleName.getText());
                    temp.getWorkers().sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
                    temp.writeSchedule();
                    scheduleName.clear(); //Clears the text field once the file is created
                    shiftList.getItems().clear();
                    workersList.getItems().clear();
                }
                else {
                    System.out.println("File already exists!");
                    //code below is opening a new error window
                    Stage errorWindow = new Stage();
                    errorWindow.setTitle("Error");
                    errorWindow.initModality(Modality.APPLICATION_MODAL);
                    errorWindow.initStyle(StageStyle.UNDECORATED);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../fxml/errorScheduleAlreadyExists.fxml"));
                    errorWindow.setScene(new Scene(loader.load()));
                    errorWindow.show();

                }
            } catch (IOException e) {
                System.out.println("An error occurred in creating a new file!");
                e.printStackTrace();
            }
        }
    }

    public void refreshWindow() {
        shiftList.setItems(FXCollections.observableList(temp.getShifts()));
        workersList.setItems(FXCollections.observableList(temp.getWorkers()));
    }

    public static Schedule getTemp() {
        return temp;
    }

    public static void setTemp(Schedule temp) {
        NewScheduleController.temp = temp;
    }

    //Opacity change on hover
    public void handleHoverButtonEnter() {
        if(buttonNewShift.isHover())
            buttonNewShift.setOpacity(0.7);
        if(buttonNewWorker.isHover())
            buttonNewWorker.setOpacity(0.7);
        if(buttonSave.isHover())
            buttonSave.setOpacity(0.7);
    }
    public void handleHoverButtonExit() {
        if(!buttonNewShift.isHover())
            buttonNewShift.setOpacity(1);
        if(!buttonNewWorker.isHover())
            buttonNewWorker.setOpacity(1);
        if(!buttonSave.isHover())
            buttonSave.setOpacity(1);
    }

}
