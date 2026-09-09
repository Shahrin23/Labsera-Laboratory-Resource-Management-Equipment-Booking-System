package com.labresa;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Central place for switching screens. Keeps a back-stack so every screen can
 * offer a working "Back" action instead of leaving the user stranded, and
 * guarantees the shared stylesheet is applied consistently on every scene
 * change rather than each controller re-implementing scene-switching itself.
 */
public class Navigator {

    private static Stage primaryStage;
    private static final Deque<String> history = new ArrayDeque<>();
    private static final String STYLESHEET = "/css/app.css";

    public static void init(Stage stage) {
        primaryStage = stage;
    }

    /** Navigate forward to a new screen, remembering the current one for Back. */
    public static void goTo(String fxmlPath, String title) {
        try {
            if (!history.isEmpty()) {
                // no-op placeholder kept for symmetry; current screen is pushed by caller via goToTracked
            }
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            Parent root = loader.load();
            Scene scene = new Scene(root, primaryStage.getScene() != null ? primaryStage.getScene().getWidth() : 1000,
                    primaryStage.getScene() != null ? primaryStage.getScene().getHeight() : 680);
            scene.getStylesheets().add(Navigator.class.getResource(STYLESHEET).toExternalForm());
            primaryStage.setScene(scene);
            if (title != null) primaryStage.setTitle(title);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load screen: " + fxmlPath, e);
        }
    }

    /** Push the current screen path onto the back-stack, then navigate to a new one. */
    public static void push(String currentFxmlPath, String nextFxmlPath, String title) {
        history.push(currentFxmlPath);
        goTo(nextFxmlPath, title);
    }

    public static boolean canGoBack() {
        return !history.isEmpty();
    }

    public static void back(String title) {
        if (!history.isEmpty()) {
            goTo(history.pop(), title);
        }
    }

    public static void clearHistory() {
        history.clear();
    }
}
