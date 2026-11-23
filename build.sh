#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JAVAFX_LIB="/usr/lib/jvm/openjfx"
TARGET_DIR="$PROJECT_DIR/target"
CLASSES_DIR="$TARGET_DIR/classes"
JAR_FILE="$TARGET_DIR/registros-biblioteca-1.0.0.jar"

echo "=========================================================="
echo "  COMPILANDO GESTOR DE BIBLIOTECA PERSONAL Y SAGAS"
echo "=========================================================="

# Limpiar directorios previos
rm -rf "$TARGET_DIR"
mkdir -p "$CLASSES_DIR"

# Encontrar archivos fuente java
SOURCES=$(find src/main/java -name "*.java")

# Compilar clases con JavaFX
echo "-> Compilando fuentes Java con JavaFX..."
javac --release 17 \
      --module-path "$JAVAFX_LIB" \
      --add-modules javafx.controls,javafx.fxml \
      -d "$CLASSES_DIR" \
      $SOURCES

# Copiar recursos (FXML, CSS)
echo "-> Copiando recursos FXML y CSS..."
cp -r src/main/resources/* "$CLASSES_DIR/"

# Generar archivo MANIFEST
MANIFEST_FILE="$TARGET_DIR/MANIFEST.MF"
echo "Manifest-Version: 1.0" > "$MANIFEST_FILE"
echo "Main-Class: com.biblioteca.Launcher" >> "$MANIFEST_FILE"

# Empaquetar JAR ejecutable
echo "-> Empaquetando JAR en $JAR_FILE..."
jar cfm "$JAR_FILE" "$MANIFEST_FILE" -C "$CLASSES_DIR" .

echo "=========================================================="
echo "  ¡COMPILACIÓN EXITOSA!"
echo "  JAR generado: target/registros-biblioteca-1.0.0.jar"
echo "=========================================================="
