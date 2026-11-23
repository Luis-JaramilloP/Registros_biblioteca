package com.biblioteca;

/**
 * Lanzador independiente para eludir verificaciones de módulos en tiempo de ejecución
 * al empaquetar en JAR ejecutable o AppImage.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
