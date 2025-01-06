package org.ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.net.Client;
import org.util.Logging;

public class GUI_Controller_Chatbox {
    private Client client;

    @FXML
    private TextField input_box;
    public void setTextfeld(String text) {
        input_box.setText(text);
    }
    public String getTextfeldContent() {
        return input_box.getText();
    }

    @FXML
    private TextArea output_field;
    public void setChatbox(String text) {
        output_field.setText(text);
    }
    public void appendChatbox(String text) {
        output_field.appendText(text + "\n");
    }

    public void senden() {
        if(input_box.getText().isEmpty()) {
            return;
        }
        if(client == null) {
            Logging.logError("Client not set! Cannot send message.");
            return;
        }
        Platform.runLater(() -> {
            Logging.logInfo("[GUI Controller] Sending message: " + input_box.getText());
            client.sendMessage(input_box.getText());
            Logging.logSuccess("[GUI Controller] Message sent!");
            input_box.setText("");
        });
    }

    public void handleEnter(KeyEvent event) {
        if(event.getCode()== KeyCode.ENTER) senden();
    }

    public void setClient(Client client) {
        this.client = client;
    }
}