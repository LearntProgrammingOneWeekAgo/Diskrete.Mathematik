package org;
import javafx.application.Application;
import javafx.stage.Stage;
import org.net.Client;
import org.net.Server;
import org.ui.GUI_Chatbox;
import org.util.Prop;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;


public class Main extends Application {
    public static ArrayBlockingQueue<String> messageStack = new ArrayBlockingQueue<>(100);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Properties properties = Prop.loadProperties();

        if(properties.getProperty("startServer").equals("true")) {
            Server server = new Server();
            server.start();
        }

        // Starte den Socket in einem Thread
        Client client = new Client();
        client.setProperties(properties);
        client.setMessageStack(messageStack);
        client.start();
        client.sendKey();
        client.listen();

        // Starte die Chat GUI
        GUI_Chatbox gui = new GUI_Chatbox();
        gui.start(stage, messageStack);
        gui.setClient(client);
    }
}