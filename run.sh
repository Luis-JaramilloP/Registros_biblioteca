#!/usr/bin/env bash
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JAVAFX_LIB="/usr/lib/jvm/openjfx"
JAR_FILE="$PROJECT_DIR/target/registros-biblioteca-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR no encontrado. Compilando primero..."
    ./build.sh
fi

# Soporte Fedora Wayland / X11
if [ -z "$GDK_BACKEND" ]; then
    export GDK_BACKEND=x11
fi
export _JAVA_AWT_WM_NONREPARENTING=1

echo "Iniciando Gestor de Biblioteca Personal y Sagas..."
exec java --enable-native-access=ALL-UNNAMED \
          --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
          --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \
          --module-path "$JAVAFX_LIB" \
          --add-modules javafx.controls,javafx.fxml \
          -jar "$JAR_FILE" "$@"
