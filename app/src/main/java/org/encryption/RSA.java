/**
 * Die Klasse RSA implementiert das RSA-Kryptosystem.
 * RSA_Encoder: Klasse zum Verschlüsseln von Nachrichten
 * RSA_Encoder(BigInteger[] publicKey): Konstruktor, der den öffentlichen Schlüssel erhält
 * RSA_Encoder(BigInteger[] publicKey, int blockSize): Konstruktor, der den öffentlichen Schlüssel und die Blockgröße erhält
 * BigInteger encrypt(BigInteger m): Verschlüsselt eine Nachricht
 * BigInteger[] encryptString(String m): Verschlüsselt eine Nachricht
 * ---------------------------------------------------
 * RSA_Decoder: Klasse zum Entschlüsseln von Nachrichten
 * RSA_Decoder(BigInteger p, BigInteger q): Konstruktor, der die Primzahlen p und q erhält
 * RSA_Decoder(): Konstruktor, der zufällige Primzahlen p und q generiert
 * BigInteger decrypt(BigInteger c): Entschlüsselt eine Nachricht
 * String decryptString(BigInteger[] c): Entschlüsselt eine Nachricht
 * BigInteger[] publicKey(): Gibt den öffentlichen Schlüssel zurück
 */
package org.encryption;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;

public class RSA {
    public static class RSA_Decoder {
        private final BigInteger p, q, n, phi,d;
        private BigInteger e;

        public RSA_Decoder(BigInteger p, BigInteger q) { // Konstruktor für den Decoder mit gegebenen Primzahlen p und q
            this.p = p;
            this.q = q;
            this.n = p.multiply(q); // n = p*q
            this.phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // phi = (p-1)*(q-1)
            this.e = BigInteger.ZERO;

            for (BigInteger i = BigInteger.TWO; i.compareTo(phi) < 0; i = i.add(BigInteger.ONE)) { //for i = 2; i < phi; i++
                if (i.gcd(phi).equals(BigInteger.ONE)) {
                    this.e = i;
                    break;
                }
            } // e ist die kleinste Zahl, die teilerfremd zu phi ist
            this.d = e.modInverse(phi); // d = e^-1 mod phi
        }

        public RSA_Decoder() { // Konstruktor für den Decoder mit zufälligen Primzahlen p und q
            SecureRandom random = new SecureRandom();
            this.p = new BigInteger(1024, 100, random);
            this.q = new BigInteger(1024, 100, random);
            this.n = p.multiply(q);
            this.phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)); // phi = (p-1)*(q-1)
            this.e = BigInteger.ZERO;

            for (BigInteger i = BigInteger.TWO; i.compareTo(phi) < 0; i = i.add(BigInteger.ONE)) { //for i = 2; i < phi; i++
                if (i.gcd(phi).equals(BigInteger.ONE)) {
                    this.e = i;
                    break;
                }
            }
            this.d = e.modInverse(phi); // d = e^-1 mod phi
        }

        // Entschlüsselt den Biginteger "c"
        public BigInteger decrypt(BigInteger c) {
            return c.modPow(d, n);
        }

        // Entschlüsselt den Biginteger Array "c" zu einem String
        public String decryptString(BigInteger[] c) {
            // Erstelle eine Liste für die entschlüsselten Bytes
            List<Byte> byteList = new ArrayList<>();
            // Iteriere über alle Blöcke
            for (BigInteger value : c) {
                // Entschlüssele den Block und konvertiere ihn in ein Byte-Array
                byte[] decryptedBlock = decrypt(value).toByteArray();
                for (byte b : decryptedBlock) {
                    if (b == 0) {// Wenn b == 0, dann überspringe den Block. Diese 0 Blöcke entstehen bei der Verschlüsselung
                        continue;
                    }
                    byteList.add(b);
                }
            }
            // Konvertiere die Liste in einen Byte-Array
            byte[] allBytes = new byte[byteList.size()];
            for (int i = 0; i < byteList.size(); i++) {
                allBytes[i] = byteList.get(i);
            }

            // Gebe das Byte-Array als String zurück
            return new String(allBytes, StandardCharsets.UTF_8);
        }

        public String decryptJSON(JSONArray content) {
            BigInteger[] encryptedContent = new BigInteger[content.length()];
            for (int i = 0; i < content.length(); i++) {
                encryptedContent[i] = new BigInteger(content.getString(i));
            }
            return this.decryptString(encryptedContent);
        }

        public publicKey givePublicKey() {
            return new publicKey(e, n);
        }
    }

    public static class RSA_Encoder {
        private final BigInteger e, n;
        private final int BLOCK_SIZE;

        // Standardkonstruktor der Klasse RSA_Encoder
        public RSA_Encoder(publicKey publicKey) {
            this.e = publicKey.e;
            this.n = publicKey.n;
            this.BLOCK_SIZE = 8;
        }

        // Konstruktor, der Klasse RSA_Encoder mit spezialisierter Blockgröße
        public RSA_Encoder(BigInteger[] publicKey, int blockSize) {
            this.e = publicKey[0];
            this.n = publicKey[1];
            this.BLOCK_SIZE = blockSize;
        }

        // Verschlüsselt einen BigInteger "m"
        public BigInteger encrypt(BigInteger m) {
            return m.modPow(e, n);
        }

        // Verschlüsselt den String "message"
        public BigInteger[] encryptString(String message) {
            // Überprüfe, ob message nur aus UTF-8 Zeichen besteht
            if(!message.equals(new String(message.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Message contains invalid characters! encryptString() only supports UTF-8 characters.");
            }

            // Konvertiere message in Byte-Array
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            // Erstelle die Liste für die verschlüsselten Blöcke
            List<BigInteger> encryptedMessage = new ArrayList<>();

            for (int i = 0; i < messageBytes.length; i += BLOCK_SIZE) {
                // Erstelle einen Block der Länge BLOCK_SIZE (Standard: 8)
                byte[] block = new byte[Math.min(BLOCK_SIZE, messageBytes.length - i)];
                // Kopiere die nächsten BLOCK_SIZE Bytes von messageBytes in block
                System.arraycopy(messageBytes, i, block, 0, block.length);
                // Konvertiere block in BigInteger
                BigInteger blockBigInt = new BigInteger(1, block);
                // Verschlüssele block und füge ihn der Liste hinzu
                encryptedMessage.add(encrypt(blockBigInt));
            }
            // Gebe die Liste als Array zurück
            return encryptedMessage.toArray(new BigInteger[0]);
        }

        public JSONArray encryptStringToJSON(String message) {
            BigInteger[] encryptedMessage = encryptString(message);
            JSONArray json = new JSONArray();
            for (BigInteger i : encryptedMessage) {
                json.put(i.toString());
            }
            return json;
        }
    }

    public static class publicKey implements Serializable {
        public BigInteger e;
        public BigInteger n;
        public publicKey(BigInteger e, BigInteger n) {
            this.e = e;
            this.n = n;
        }
        public publicKey(JSONObject json) {
            this.e = new BigInteger(json.get("e").toString());
            this.n = new BigInteger(json.get("n").toString());
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("type", "key");
            json.put("e", e.toString());
            json.put("n", n.toString());
            return json;
        }
    }
}