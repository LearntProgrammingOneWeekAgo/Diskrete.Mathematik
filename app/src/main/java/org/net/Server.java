package org.net;
import org.encryption.RSA;
import org.json.JSONArray;
import org.json.JSONObject;
import org.util.Logging;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    public int port = 12345;
    public ServerSocket server;
    private final Map<Socket,ClientInfo> clients = new HashMap<>();
    private final RSA.RSA_Decoder decoder = new RSA.RSA_Decoder();
    public Server() {
        try {
            server = new ServerSocket(port);
        } catch (Exception e) {
            Logging.logError("[Server] Error creating server: " + e.getMessage());
        }
    }

    public void start() {
        Logging.logSuccess("[Server] Server started on port " + port);
        listen();
    }

    public void listen() {
        new Thread(() -> {
            Logging.logInfo("[Server] Listening for clients...");
            while (true) {
                try {
                    Socket client = server.accept();
                    ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                    clients.put(client, new ClientInfo());
                    clients.get(client).setObjOut(out);
                    Logging.logSuccess("[Server] Client connected!");
                    handleClient(client);
                } catch (Exception e) {
                    Logging.logError("[Server] Error accepting client: " + e.getMessage());
                }
            }
        }).start();
    }

    public void handleClient(Socket client) {
        new Thread(() -> {
            try {
                ObjectOutputStream out = clients.get(client).getObjOut();
                out.writeObject(decoder.givePublicKey().toJSON().toString());
                out.flush();

                ObjectInputStream in = new ObjectInputStream(client.getInputStream());
                while (true) {
                    Object obj = in.readObject();
                    JSONObject message = new JSONObject((String) obj);
                    if(message.get("type").equals("key")) {
                        RSA.publicKey key = new RSA.publicKey(new BigInteger(message.getString("e")), new BigInteger(message.getString("n")));
                        clients.get(client).setKey(key);
                        clients.get(client).createEncoder();
                        Logging.logSuccess("[Server] Received public key from client!");
                        continue;
                    }
                    if(message.get("type").equals("message")) {
                        Logging.logInfo("[Server] Received message from client...");
                        broadcast(message);
                    }
                }
            } catch (Exception e) {
                Logging.logError("Error handling client: " + e.getMessage());
                try { Thread.sleep(1000); } catch (InterruptedException ex) { throw new RuntimeException(ex); }
            }
        }).start();
    }

    public void broadcast(JSONObject message) {
        Logging.logInfo("[Server] Broadcasting message: ");
        JSONArray arr = message.getJSONArray("content");
        BigInteger[] encryptedContent2 = new BigInteger[arr.length()];
        for (int i = 0; i < arr.length(); i++) {
            encryptedContent2[i] = new BigInteger(arr.getString(i));
        }
        String content = decoder.decryptString(encryptedContent2);

        for (Map.Entry<Socket,ClientInfo> entry : clients.entrySet()) {
            try {
                if(entry.getKey()==null) continue;
                ObjectOutputStream out = entry.getValue().getObjOut();
                JSONArray encryptedContent = new JSONArray();
                for (BigInteger i : entry.getValue().getEncoder().encryptString(content)) {
                    encryptedContent.put(i.toString());
                }
                message.remove("content");
                message.put("content",encryptedContent);
                out.writeObject(message.toString());
                out.flush();
            } catch (Exception e) {
                Logging.logError("Error broadcasting message: " + e.getMessage());
            }
        }
        Logging.logInfo("[Server] Broadcasted message!");
    }

    public static class ClientInfo {
        private ObjectOutputStream out;
        private RSA.publicKey key;
        private RSA.RSA_Encoder encoder;
        public void setObjOut(ObjectOutputStream out) {
            this.out = out;
        }
        public void setKey(RSA.publicKey key) {
            this.key = key;
        }
        public ObjectOutputStream getObjOut() {
            return out;
        }
        public RSA.publicKey getKey() { return key; }
        public RSA.RSA_Encoder getEncoder() {
            return encoder;
        }
        public void createEncoder() {
            encoder = new RSA.RSA_Encoder(key);
        }
    }
}