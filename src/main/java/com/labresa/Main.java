package com.labresa;

import com.labresa.db.DatabaseSeeder;
import com.labresa.facade.LabResaFacade;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private static final LabResaFacade facade = new LabResaFacade();

    public static LabResaFacade getFacade() {
        return facade;
    }

    @Override
    public void start(Stage stage) {
        DatabaseSeeder.run();

        Navigator.init(stage);
        stage.setTitle("LabResa - Laboratory Resource & Equipment Booking System");
        Navigator.goTo("/fxml/login.fxml", null);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
