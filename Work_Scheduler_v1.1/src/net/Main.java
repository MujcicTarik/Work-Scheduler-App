package net;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.LocalDateStringConverter;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class Main extends Application {
    double x, y;

    @Override
    public void start(Stage primaryStage) throws Exception {

        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/sample.fxml"));
            primaryStage.initStyle(StageStyle.UNDECORATED); //makes the window empty

            //6 next lines are setting the app to be maximized when opened since you can't do it with primaryStage.setMaximized(true);
            //since then it's covering the whole screen and you can't see the windows taskbar

        /*Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());*/

            //the section below which has 2 functions is for making the window movable/draggable on
            // the screen and I think it's better for it to be in the same position

            root.setOnMousePressed(event -> {
                x = event.getSceneX();
                y = event.getSceneY();
            });

            root.setOnMouseDragged(event -> {
                primaryStage.setX(event.getScreenX() - x);
                primaryStage.setY(event.getScreenY() - y);
            });

            //these 2 lines get the screen actually showing
            primaryStage.setScene(new Scene(root));
            primaryStage.show();




        } catch(IOException e){
            e.printStackTrace();
        }

}


    public static void main(String[] args) {
        launch(args);
    }
}
