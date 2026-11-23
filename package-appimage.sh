#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JAVAFX_LIB="/usr/lib/jvm/openjfx"
TARGET_DIR="$PROJECT_DIR/target"
DIST_DIR="$PROJECT_DIR/dist"
APPDIR="$DIST_DIR/BibliotecaPersonal.AppDir"
JAR_FILE="$TARGET_DIR/registros-biblioteca-1.0.0.jar"
APPIMAGE_OUTPUT="$DIST_DIR/BibliotecaPersonal-x86_64.AppImage"

echo "=========================================================="
echo "  EMPAQUETANDO APLICACIÓN LINUX (APPIMAGE STANDALONE)"
echo "=========================================================="

# Asegurar compilación
if [ ! -f "$JAR_FILE" ]; then
    ./build.sh
fi

rm -rf "$DIST_DIR"
mkdir -p "$APPDIR/usr/bin"
mkdir -p "$APPDIR/usr/lib/openjfx"
mkdir -p "$APPDIR/usr/share/icons/hicolor/scalable/apps"

# Copiar el JAR principal
cp "$JAR_FILE" "$APPDIR/usr/bin/biblioteca.jar"

# Copiar librerías JavaFX al paquete portable
echo "-> Copiando librerías JavaFX..."
cp -r "$JAVAFX_LIB"/*.jar "$APPDIR/usr/lib/openjfx/"
if ls "$JAVAFX_LIB"/*.so 1> /dev/null 2>&1; then
    cp "$JAVAFX_LIB"/*.so "$APPDIR/usr/lib/openjfx/"
fi

# Crear icono SVG
cat << 'EOF' > "$APPDIR/biblioteca.svg"
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 128 128" width="128" height="128">
  <rect width="128" height="128" rx="24" fill="#1e293b"/>
  <path d="M30 32 C30 32, 54 28, 64 38 C74 28, 98 32, 98 32 L98 94 C98 94, 74 90, 64 98 C54 90, 30 94, 30 94 Z" fill="#38bdf8" opacity="0.9"/>
  <path d="M64 38 L64 98" stroke="#0f172a" stroke-width="3"/>
  <circle cx="64" cy="24" r="6" fill="#fbbf24"/>
</svg>
EOF
cp "$APPDIR/biblioteca.svg" "$APPDIR/.DirIcon"
cp "$APPDIR/biblioteca.svg" "$APPDIR/usr/share/icons/hicolor/scalable/apps/biblioteca.svg"

# Crear el lanzador interno AppRun con soporte Wayland y registro de diagnóstico
cat << 'EOF' > "$APPDIR/AppRun"
#!/usr/bin/env bash
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
export LD_LIBRARY_PATH="$HERE/usr/lib/openjfx:$LD_LIBRARY_PATH"

# Soporte Fedora Wayland / X11 para GTK3 Glass
if [ -z "$GDK_BACKEND" ]; then
    export GDK_BACKEND=x11
fi
export _JAVA_AWT_WM_NONREPARENTING=1

LOG_FILE="/tmp/biblioteca_app.log"
echo "=== Sesión iniciada: $(date) ===" > "$LOG_FILE"
echo "WAYLAND_DISPLAY: $WAYLAND_DISPLAY" >> "$LOG_FILE"
echo "DISPLAY: $DISPLAY" >> "$LOG_FILE"
echo "GDK_BACKEND: $GDK_BACKEND" >> "$LOG_FILE"

if command -v java >/dev/null 2>&1; then
    JAVA_CMD="java"
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    MSG="No se encontró Java (JRE 17+) en el sistema para ejecutar."
    echo "$MSG" >> "$LOG_FILE"
    zenity --error --title="Biblioteca Personal" --text="$MSG" 2>/dev/null || true
    exit 1
fi

"$JAVA_CMD" \
     --enable-native-access=ALL-UNNAMED \
     --add-opens javafx.graphics/com.sun.glass.ui=ALL-UNNAMED \
     --add-opens javafx.fxml/javafx.fxml=ALL-UNNAMED \
     --module-path "$HERE/usr/lib/openjfx" \
     --add-modules javafx.controls,javafx.fxml \
     -jar "$HERE/usr/bin/biblioteca.jar" "$@" >> "$LOG_FILE" 2>&1

EXIT_CODE=$?
if [ $EXIT_CODE -ne 0 ]; then
    ERROR_DETAIL=$(tail -n 12 "$LOG_FILE")
    zenity --error --title="Biblioteca Personal" \
           --text="Error al ejecutar la aplicación (código $EXIT_CODE).\n\nDetalles:\n$ERROR_DETAIL\n\nArchivo de registro: $LOG_FILE" 2>/dev/null || true
fi
exit $EXIT_CODE
EOF
chmod +x "$APPDIR/AppRun"

# Crear archivo .desktop
cat << 'EOF' > "$APPDIR/biblioteca.desktop"
[Desktop Entry]
Version=1.0
Type=Application
Name=Biblioteca Personal y Sagas
Comment=Gestor de biblioteca personal y sagas literarias con persistencia en archivos
Exec=AppRun
Icon=biblioteca
Terminal=false
Categories=Office;Database;Utility;Java;
StartupWMClass=com.biblioteca.MainApp
EOF

# Empaquetar todo el AppDir en un archivo único autoejecutable AppImage
echo "-> Generando archivo único autoejecutable AppImage..."
PAYLOAD_TAR="$DIST_DIR/payload.tar.gz"
tar -czf "$PAYLOAD_TAR" -C "$APPDIR" .

cat << 'EOF' > "$APPIMAGE_OUTPUT"
#!/usr/bin/env bash
# ====================================================================
# Gestor de Biblioteca Personal y Sagas - AppImage Portable para Linux
# ====================================================================
set -e

# Resolver ruta canónica del propio script
SELF=$(realpath "$0" 2>/dev/null || readlink -f "$0" 2>/dev/null || echo "$0")

# Directorio de extracción temporal
RUN_DIR=$(mktemp -d /tmp/biblioteca_app_XXXXXX)
cleanup() {
    rm -rf "$RUN_DIR"
}
trap cleanup EXIT INT TERM

# Extraer el contenido comprimido interno
ARCHIVE_START=$(awk '/^__PAYLOAD_ARCHIVE_BEGINS_HERE__/ {print NR + 1; exit 0; }' "$SELF")
tail -n +"$ARCHIVE_START" "$SELF" | tar -xz -C "$RUN_DIR"

# Ejecutar AppRun dentro del contenedor temporal
exec "$RUN_DIR/AppRun" "$@"

__PAYLOAD_ARCHIVE_BEGINS_HERE__
EOF

cat "$PAYLOAD_TAR" >> "$APPIMAGE_OUTPUT"
rm -f "$PAYLOAD_TAR"
chmod +x "$APPIMAGE_OUTPUT"

echo "=========================================================="
echo "  ¡APPIMAGE GENERADO CON ÉXITO!"
echo "  Archivo único: dist/BibliotecaPersonal-x86_64.AppImage"
echo "  Tamaño: $(du -h "$APPIMAGE_OUTPUT" | cut -f1)"
echo "=========================================================="
