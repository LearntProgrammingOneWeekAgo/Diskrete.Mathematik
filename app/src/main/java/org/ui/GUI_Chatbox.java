package org.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.net.Client;
import org.util.Logging;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

public class GUI_Chatbox {
    public GUI_Controller_Chatbox controllerChatbox;
    public ArrayBlockingQueue<String> messageStack;
    public GUI_Chatbox() {

    }

    public void start(Stage stage, ArrayBlockingQueue<String> messageStack) {
        this.messageStack = messageStack;
        // JavaFX muss auf dem JavaFX-Thread ausgeführt werden, Platform.runLater() garantiert das
        Platform.runLater(() -> {
            FXMLLoader loader;
            Parent root;
            try {
                loader = new FXMLLoader(GUI_Chatbox.class.getResource("/ui/fxml/frame_chatbox.fxml"));
                // Object.requireNonNull() stellt sicher, dass die GUI Datei vorhanden ist
                root = Objects.requireNonNull(loader.load());
                Logging.logSuccess("[GUI] FXML loaded.");
            } catch (Exception e) {
                Logging.logError("Failed to load FXML. Quitting.");
                return;
            }
            controllerChatbox = loader.getController();
            Scene scene = new Scene(root);

            try {
                Image icon = new Image("/window/icon.png");
                stage.getIcons().add(icon);
                Logging.logSuccess("[GUI] Icon loaded.");
            } catch (Exception e) {
                Logging.logError("[GUI] Failed to load icon.");
            }
            try {
                scene.getStylesheets().add(getClass().getResource("/ui/css/styling.css").toExternalForm());
                Logging.logSuccess("[GUI] CSS loaded.");
            } catch (Exception e) {
                Logging.logError("[GUI] Failed to load CSS.");
            }
            stage.setTitle("RSA Messenger");
            stage.setScene(scene);
            Logging.logInfo("[GUI] Starting main GUI.");
            stage.show();
        });

        // Thread prüft nach neuen Nachrichten und fügt sie der Chatbox hinzu
        new Thread(() -> {
            while (true) {
                Platform.runLater(() -> {
                    if (!messageStack.isEmpty()) {
                        controllerChatbox.appendChatbox(messageStack.poll());
                        Logging.logInfo("[GUI] Appending Message.");
                    }
                });
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public GUI_Controller_Chatbox getController() {
        if(controllerChatbox == null) {
            Logging.logError("[GUI] Controller not initialized.");
            return null;
        }
        return controllerChatbox;
    }

    public void setClient(Client client) {
        Platform.runLater(() -> controllerChatbox.setClient(client));
    }
}