package net.controllers;


import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.schedule.Schedule;
import net.schedule.Shift;
import net.worker.Worker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class EditScheduleController implements Initializable {

    @FXML
    JFXListView schedulesList;

    @FXML
    JFXButton editButton, deleteButton;

    @FXML
    JFXButton buttonSave;

    @FXML
    JFXListView workersList;

    @FXML
    JFXListView shiftList;

    @FXML
    JFXButton buttonNewWorker, buttonNewShift;

    @FXML
    TextField scheduleName;

    double x,y; //help variables for taking position on the window

    static Worker selectedWorker;
    static Shift selectedShift;

    static String selectedSchedule;
    static Schedule temp = new Schedule();
    //


    private ArrayList<String> fileNames = new ArrayList<String>();

    public static boolean isDeleteSelected = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buttonSave.setDisable(true);
        buttonNewShift.setDisable(true);
        buttonNewWorker.setDisable(true);
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        File dir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator);
        File[] files = dir.listFiles();
        for(File file : files){
            if(file.isDirectory()) {
                String string = file.getName();
                fileNames.add(string);
            }
        }
        schedulesList.setItems(FXCollections.observableList(fileNames));
        schedulesList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                //to handle a double click
                if(event.getClickCount() == 2){
                    handleEdit();
                }
            }
        });


        workersList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2){
                    selectedWorker = (Worker) workersList.getSelectionModel().getSelectedItem();
                    System.out.println(selectedWorker);
                    try {
                        handleEditWorker();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        shiftList.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getClickCount() == 2){
                    selectedShift = (Shift) shiftList.getSelectionModel().getSelectedItem();
                    System.out.println(selectedShift);
                    try {
                        handleEditShift(selectedShift);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });


    }

    public void handleEdit(){
        if(!(schedulesList.getSelectionModel().getSelectedItem() == null)) {
            selectedSchedule = schedulesList.getSelectionModel().getSelectedItem().toString();
            System.out.println(selectedSchedule);
            try {
                temp = Schedule.readSchedule(selectedSchedule);
                shiftList.setItems(FXCollections.observableList(temp.getShifts()));

                shiftList.setCellFactory(sL -> {
                    JFXListCell<Shift> cell = new JFXListCell<Shift>();
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem editItem = new MenuItem();
                    editItem.textProperty().set("Edit");
                  //  editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
                    editItem.setOnAction(event -> {
                        selectedShift = cell.getItem();
                        try {
                            handleEditShift(selectedShift);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }});
                    MenuItem deleteItem = new MenuItem();
                    deleteItem.textProperty().set("Delete");
                   // deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
                    deleteItem.setOnAction(event -> {
                        selectedShift = cell.getItem();
                        deleteShift(selectedShift);
                    });
                    contextMenu.getItems().addAll(editItem, deleteItem);
                    cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                        if (isNowEmpty) {
                            cell.setContextMenu(null);
                        } else {
                            cell.setContextMenu(contextMenu);
                        }
                    });
                    return cell ;

                });
                workersList.setCellFactory(sL -> {
                    JFXListCell<Worker> cell = new JFXListCell<Worker>();
                    ContextMenu contextMenu = new ContextMenu();

                    MenuItem editItem = new MenuItem();
                    editItem.textProperty().set("Edit");
                   // editItem.textProperty().bind(Bindings.format("Edit \"%s\"", cell.itemProperty()));
                    editItem.setOnAction(event -> {
                        selectedWorker = cell.getItem();
                        try {
                            handleEditWorker();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }});
                    MenuItem deleteItem = new MenuItem();
                    deleteItem.textProperty().set("Delete");
                    //deleteItem.textProperty().bind(Bindings.format("Delete \"%s\"", cell.itemProperty()));
                    deleteItem.setOnAction(event -> {
                        selectedWorker = cell.getItem();
                        deleteWorker(selectedWorker);
                    });
                    contextMenu.getItems().addAll(editItem, deleteItem);
                    cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                        if (isNowEmpty) {
                            cell.setContextMenu(null);
                        } else {
                            cell.setContextMenu(contextMenu);
                        }
                    });
                    return cell ;

                });
                workersList.setItems(FXCollections.observableList(temp.getWorkers()));
                scheduleName.setText(temp.getScheduleName());
                buttonSave.setDisable(false);
                buttonNewWorker.setDisable(false);
                buttonNewShift.setDisable(false);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    public void deleteShift(Shift shift){
        EditScheduleController.getSchedule().getShifts().remove(shift);
        shiftList.getItems().remove(shift);
        for(Worker w : temp.getWorkers()){
            if(w.getShifts().contains(shift.getName()))
                w.getShifts().remove(shift.getName());
        }
    }
    public void deleteWorker(Worker worker){
        EditScheduleController.getSchedule().getWorkers().remove(worker);
        workersList.getItems().remove(worker);
    }
    public void handleDelete() throws IOException {

        //are you sure you want to delete Window
        buttonSave.setDisable(true);
        buttonNewShift.setDisable(true);
        buttonNewWorker.setDisable(true);
        Stage newWindow = new Stage();
        newWindow.setTitle("Are you sure you want to delete?");
        newWindow.initModality(Modality.APPLICATION_MODAL);
        newWindow.initStyle(StageStyle.UNDECORATED);

        Parent loader = FXMLLoader.load(getClass().getResource("../fxml/confirmDeleteSchedule.fxml"));
        newWindow.setScene(new Scene(loader));
        newWindow.show();

        /*if(isDeleteSelected == true) {
            System.out.println("HEHE");

            //at the end change isDeleteSelected to false
            isDeleteSelected = false;
        }*/

    }




    public void handleEditWorker() throws IOException {
        Stage newWindow = new Stage();
        newWindow.setTitle("Edit Worker");
        newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
        newWindow.initStyle(StageStyle.UNDECORATED);

//Create view from FXML
        Parent loader = FXMLLoader.load(getClass().getResource("../fxml/editWorker.fxml"));

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
    public void handleEditShift(Shift shift) throws IOException {

        Stage newWindow = new Stage();
        newWindow.setTitle("Edit Shift");
        newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
        newWindow.initStyle(StageStyle.UNDECORATED);

//Create view from FXML
        Parent loader = FXMLLoader.load(getClass().getResource("../fxml/editShift.fxml"));

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

    public void handleNewShift() throws IOException {

        System.out.println(temp.getScheduleName());
        Stage newWindow = new Stage();
        newWindow.setTitle("Add Shift");
        newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
        newWindow.initStyle(StageStyle.UNDECORATED);

//Create view from FXML
        Parent loader = FXMLLoader.load(getClass().getResource("../fxml/addNewShiftEDIT.fxml"));
        //making window draggable
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
            Parent loader = FXMLLoader.load(getClass().getResource("../fxml/errorNoShiftsCreated.fxml"));

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
        else {
            Stage newWindow = new Stage();
            newWindow.setTitle("Add Worker");
            newWindow.initModality(Modality.APPLICATION_MODAL); //blocks other windows from interacting
            newWindow.initStyle(StageStyle.UNDECORATED);
            //Create view from FXML
            Parent loader = FXMLLoader.load(getClass().getResource("../fxml/addNewWorkerEDIT.fxml"));

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

        //save new things to schedule object
        if (scheduleName.getText().isEmpty()) {  //to make sure that you can't create a schedule that has no name
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
        } else {
            String oldName = temp.getScheduleName();
            System.out.println("OLD NAME: " + oldName);
            //save the current schedule to Object
            //selectedSchedule = schedulesList.getSelectionModel().getSelectedItem().toString();
            temp.setScheduleName(scheduleName.getText());
            //JFXListView -> ArrayList<Shift>
            temp.setShifts(new ArrayList<>(shiftList.getItems()));
            temp.setWorkers(new ArrayList<>(workersList.getItems()));

            //delete the old file for the object
            //OLD DIRECTORY
            File oldDir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + oldName);
            //NEW DIRECTOYRY
            File newDir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + temp.getScheduleName());
            //OLD TXT FILE FOR SCHEDULE
            File oldFileTxt = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator  + temp.getScheduleName() + File.separator + oldName + ".txt");
            /*//NEW TXT FILE FOR SCHEDULE
            File newFileTxt = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator + temp.getScheduleName() + File.separator + temp.getScheduleName() + ".txt");*/

            oldDir.renameTo(newDir);
            System.out.println("OLD FILE: " + oldFileTxt);
            if(oldFileTxt.delete()){
                System.out.println("File " + oldName + ".txt deleted successfully");
            }
            else{
                System.out.println("Error deleting the file " + oldName + ".txt");
            }

            try {
                    temp.getWorkers().sort((e, v) -> e.getFirstname().compareTo(v.getFirstname()));
                    temp.writeSchedule();

                    //open a notification Window that the changes were successfully saved
                    Stage confimationWindow = new Stage();
                    confimationWindow.setTitle("Success");
                    confimationWindow.initModality(Modality.APPLICATION_MODAL);
                    confimationWindow.initStyle(StageStyle.UNDECORATED);

                    Parent loader = FXMLLoader.load(getClass().getResource("../fxml/changesSavedSuccessfully.fxml"));
                    confimationWindow.setScene(new Scene(loader));
                    confimationWindow.show();

            } catch (IOException ex) {
                System.out.println("An error occured in creating a new file");
                ex.printStackTrace();
            }
            //make everything on Window empty since it's deleted
            scheduleName.clear();
            shiftList.getItems().clear();
            workersList.getItems().clear();
            buttonSave.setDisable(true);
            buttonNewShift.setDisable(true);
            buttonNewWorker.setDisable(true);
            editButton.setDisable(true);
            deleteButton.setDisable(true);
        }

    }




    //Opacity hover change
    public void handleHoverButtonEnter() {
        double hover = 0.7; //to make changes easier
        if(editButton.isHover())
            editButton.setOpacity(hover);
        if(buttonNewShift.isHover())
            buttonNewShift.setOpacity(hover);
        if(buttonNewWorker.isHover())
            buttonNewWorker.setOpacity(hover);
        if(deleteButton.isHover())
            deleteButton.setOpacity(hover);
        if(buttonSave.isHover())
            buttonSave.setOpacity(hover);
    }
    public void handleHoverButtonExit() {
        if(!editButton.isHover())
            editButton.setOpacity(1);
        if(!buttonNewShift.isHover() && !buttonNewShift.isDisabled())
            buttonNewShift.setOpacity(1);
        if(!buttonNewWorker.isHover() && !buttonNewWorker.isDisabled())
            buttonNewWorker.setOpacity(1);
        if(!deleteButton.isHover())
            deleteButton.setOpacity(1);
        if(!buttonSave.isHover() && !buttonSave.isDisabled())
            buttonSave.setOpacity(1);
    }

    public static Worker getSelectedWorker() {
        return selectedWorker;
    }

    public static void setSelectedWorker(Worker selectedWorker) {
        EditScheduleController.selectedWorker = selectedWorker;
    }

    public static Shift getSelectedShift() {
        return selectedShift;
    }

    public static void setSelectedShift(Shift selectedShift) {
        EditScheduleController.selectedShift = selectedShift;
    }

    public static Schedule getSchedule() {
        return temp;
    }

    //help function for deleting the directory
    public void deleteDir(File file) throws IOException {
        if(file.isDirectory()) {
            //if directory is empty delete it
            if(file.list().length == 0)
                file.delete();
            else{
                //list all the directory contents
                File files[] = file.listFiles();
                for(File fileDelete: files){
                    //recursive delete
                    deleteDir(fileDelete);
                }
                //check the directory again, ifEmpty then delete it
                if(file.list().length == 0)
                    file.delete();
            }
        }
        else {
            //if it's a file and not directory, delete it
            file.delete();
        }
    }



    public void refreshWindow() throws IOException {

        if (isDeleteSelected == true) {
            System.out.println("refreshWindow() : isDeleteSelected = true");
            File dir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator);
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    String currDirName = file.getName();
                    if (currDirName.equals(schedulesList.getSelectionModel().getSelectedItem().toString())) {
                        //deletion of the directory
                        deleteDir(file);
                    }
                }
            }
            //make everything on Window empty since it's deleted
            scheduleName.clear();
            shiftList.getItems().clear();
            workersList.getItems().clear();
        }
        //at the end change isDeleteSelected to false
        isDeleteSelected = false;

        //to refresh current schedules
        schedulesList.setItems(null);
        File dir = new File("src" + File.separator + "net" + File.separator + "data" + File.separator + "schedules" + File.separator);
        File[] files = dir.listFiles();
        fileNames.removeAll(fileNames); //this line emties the fileNames ArrayList
        //for loop below reads the file from the schedule directory and adds them to fileNames
        for(File file : files){
            if(file.isDirectory()) {
                String string = file.getName();
                fileNames.add(string);
            }
        }
        //this shows fileNames in the JFXListView after they are updated by the code above
        schedulesList.setItems(FXCollections.observableList(fileNames));

        shiftList.setItems(FXCollections.observableList(temp.getShifts()));
        workersList.setItems(FXCollections.observableList(temp.getWorkers()));
    }




    public static void setSchedule(Schedule schedule) {
        EditScheduleController.temp = schedule;
    }
}
