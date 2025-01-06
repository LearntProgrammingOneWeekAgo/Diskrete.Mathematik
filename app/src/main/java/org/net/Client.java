package org.net;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;

import org.encryption.RSA;
import org.json.JSONObject;
import org.util.Logging;

public class Client {
    private Properties properties;
    Socket client;

    private ObjectOutputStream out;
    private ObjectInputStream in;

    private ArrayBlockingQueue<String> messageStack;
    private RSA.RSA_Decoder decoder;
    private RSA.RSA_Encoder encoder;

    public void start() {
        client = new Socket();
        String ip = properties.getProperty("host");
        int port = Integer.parseInt(properties.getProperty("port"));
        decoder = new RSA.RSA_Decoder();
        try {
            Logging.logSuccess("[Client] Attempting to connect to server...");
            client.connect(new InetSocketAddress(ip, port));
            Logging.logSuccess("[Client] Connected to server!");
            out = new ObjectOutputStream(client.getOutputStream());
            Logging.logSuccess("[Client] Output stream initialized.");
            in = new ObjectInputStream(client.getInputStream());
            Logging.logSuccess("[Client] Input stream initialized!");
        } catch (IOException e) {
            Logging.logError("[Client] Error connecting to server: " + e.getMessage());
        }
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public void setMessageStack(ArrayBlockingQueue<String> messageStack) {
        this.messageStack = messageStack;
    }

    public void listen() {
        new Thread(() -> {
            try {
                while (true) {
                    JSONObject message = new JSONObject((String) in.readObject());
                    if (message.getString("type").equals("message")) {
                        // Dekodiere die verschlüsselte Nachricht
                        String out = String.format("%s: %s", message.getString("sender"),decoder.decryptJSON(message.getJSONArray("content")));
                        messageStack.add(out);
                    } else if (message.getString("type").equals("key")) {
                        RSA.publicKey key = new RSA.publicKey(message);
                        encoder = new RSA.RSA_Encoder(key);
                        Logging.logSuccess("[Client] Received public key from server!");
                    }
                }
            } catch (Exception e) {
                Logging.logError("[Client] Error listening for messages: " + e.getMessage());
            }
        }).start();
    }

    public void sendMessage(String message) {
        JSONObject messageObject = new JSONObject();
        messageObject.put("type", "message");
        messageObject.put("sender", properties.getProperty("username"));
        messageObject.put("content",encoder.encryptStringToJSON(message));

        try {
            out.writeObject(messageObject.toString());
            out.flush();
            Logging.logInfo("[Client] Sent message: " + message);
        } catch (Exception e) {
            Logging.logError("[Client] Error sending message: " + e.getMessage());
        }
    }

    public void sendKey() {
        try {
            out.writeObject(decoder.givePublicKey().toJSON().toString());
            out.flush();
            Logging.logSuccess("[Client] Sent key!");
        } catch (Exception e) {
            Logging.logError("[Client] Error sending key: " + e.getMessage());
        }
    }
}
