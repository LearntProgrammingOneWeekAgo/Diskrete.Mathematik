package org;
public class Launcher {
    // Dieser Umweg wird benötigt, da Main.java nicht ohne weiteres in der kompilierten Uber Jar ausgeführt werden kann
    // Sonst wirft die .jar folgende Exception: JavaFX Runtime Komponenten fehlen.
    // NICHT ÄNDERN
    public static void main(final String[] args) {
        Main.main(args);
    }
}