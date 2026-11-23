package com.biblioteca.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestiona las rutas de almacenamiento de datos en el sistema Linux.
 * Sigue el estándar XDG Base Directory (~/.local/share/biblioteca_personal/)
 * con respaldo a un directorio local si no se cuenta con permisos de escritura.
 */
public final class StorageConfig {

    private static final String APP_FOLDER = "biblioteca_personal";
    private static Path dataDirectory;

    private StorageConfig() {}

    public static synchronized Path getDataDirectory() {
        if (dataDirectory == null) {
            Path targetDir = resolvePreferredDirectory();
            try {
                if (!Files.exists(targetDir)) {
                    Files.createDirectories(targetDir);
                }
                dataDirectory = targetDir;
            } catch (Exception e) {
                // Si falla la ruta preferida, utilizar carpeta local 'data' en el directorio de trabajo
                Path fallback = Paths.get(System.getProperty("user.dir"), "data");
                try {
                    Files.createDirectories(fallback);
                    dataDirectory = fallback;
                } catch (Exception ex) {
                    throw new IllegalStateException("No se pudo inicializar ningún directorio de datos", ex);
                }
            }
        }
        return dataDirectory;
    }

    private static Path resolvePreferredDirectory() {
        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        if (xdgDataHome != null && !xdgDataHome.isBlank()) {
            return Paths.get(xdgDataHome, APP_FOLDER);
        }
        String userHome = System.getProperty("user.home");
        if (userHome != null && !userHome.isBlank()) {
            return Paths.get(userHome, ".local", "share", APP_FOLDER);
        }
        return Paths.get(System.getProperty("user.dir"), "data");
    }

    public static Path getLibrosFilePath() {
        return getDataDirectory().resolve("libros.dat");
    }

    public static Path getSagasFilePath() {
        return getDataDirectory().resolve("sagas.dat");
    }
}
