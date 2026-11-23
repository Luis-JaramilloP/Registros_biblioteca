#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=========================================================="
echo "  INSTALADOR DE ESCRITORIO PARA FEDORA / LINUX"
echo "  Biblioteca Personal y Sagas"
echo "=========================================================="

# 1. Compilar y empaquetar si no existen
if [ ! -f "$PROJECT_DIR/target/registros-biblioteca-1.0.0.jar" ]; then
    ./build.sh
fi
if [ ! -d "$PROJECT_DIR/dist/BibliotecaPersonal.AppDir" ]; then
    ./package-appimage.sh
fi

# Rutas estándar en Linux / Fedora
APPS_DIR="$HOME/.local/share/applications"
ICONS_DIR="$HOME/.local/share/icons/hicolor/scalable/apps"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="$HOME/Escritorio"
[ -d "$DESKTOP_DIR" ] || DESKTOP_DIR="$HOME/Desktop"
APP_INSTALL_DIR="$HOME/.local/share/biblioteca_personal/app"

echo "-> Creando carpetas de usuario..."
mkdir -p "$APPS_DIR"
mkdir -p "$ICONS_DIR"
mkdir -p "$BIN_DIR"
mkdir -p "$APP_INSTALL_DIR"

# Copiar aplicación completa a la carpeta de usuario permanente
echo "-> Instalando binarios de la aplicación..."
cp -r "$PROJECT_DIR/dist/BibliotecaPersonal.AppDir/"* "$APP_INSTALL_DIR/"
chmod +x "$APP_INSTALL_DIR/AppRun"

# Crear lanzador ejecutable en ~/.local/bin/biblioteca-personal
echo "-> Creando ejecutable ~/.local/bin/biblioteca-personal..."
cat << 'EOF' > "$BIN_DIR/biblioteca-personal"
#!/usr/bin/env bash
# Soporte Wayland / X11 para Fedora GNOME
if [ -z "$GDK_BACKEND" ]; then
    export GDK_BACKEND=x11
fi
export _JAVA_AWT_WM_NONREPARENTING=1

LOG_FILE="/tmp/biblioteca_app.log"
echo "=== Ejecución desde menú de Fedora: $(date) ===" > "$LOG_FILE"

APP_DIR="$HOME/.local/share/biblioteca_personal/app"

if [ -f "$APP_DIR/AppRun" ]; then
    exec "$APP_DIR/AppRun" "$@"
else
    zenity --error --title="Biblioteca Personal" \
           --text="No se encontró la aplicación en $APP_DIR" 2>/dev/null || true
    exit 1
fi
EOF
chmod +x "$BIN_DIR/biblioteca-personal"

# Instalar icono oficial
echo "-> Instalando icono..."
cp "$PROJECT_DIR/dist/BibliotecaPersonal.AppDir/biblioteca.svg" "$ICONS_DIR/biblioteca-personal.svg"

# Crear archivo .desktop en el menú de aplicaciones de Fedora
echo "-> Creando entrada de menú..."
DESKTOP_FILE="$APPS_DIR/biblioteca-personal.desktop"
cat << EOF > "$DESKTOP_FILE"
[Desktop Entry]
Version=1.0
Type=Application
Name=Biblioteca Personal y Sagas
GenericName=Gestor de Libros
Comment=Gestor de biblioteca personal y sagas literarias con persistencia en archivos
Exec=$BIN_DIR/biblioteca-personal
Icon=biblioteca-personal
Terminal=false
Categories=Office;Database;Utility;Java;
StartupWMClass=com.biblioteca.MainApp
Keywords=Libros;Sagas;Lectura;Biblioteca;Books;
EOF
chmod +x "$DESKTOP_FILE"

# Si existe carpeta de escritorio (Escritorio o Desktop), copiar acceso directo también
if [ -d "$DESKTOP_DIR" ]; then
    cp "$DESKTOP_FILE" "$DESKTOP_DIR/biblioteca-personal.desktop"
    chmod +x "$DESKTOP_DIR/biblioteca-personal.desktop"
    gio set "$DESKTOP_DIR/biblioteca-personal.desktop" metadata::trusted true 2>/dev/null || true
fi

# Actualizar base de datos de escritorio de Fedora
update-desktop-database "$APPS_DIR" 2>/dev/null || true
gtk-update-icon-cache -f -t "$HOME/.local/share/icons/hicolor" 2>/dev/null || true

echo ""
echo "=========================================================="
echo "  ¡INSTALACIÓN COMPLETADA EXITOSAMENTE EN FEDORA!"
echo "=========================================================="
echo "  1. Presiona la tecla Windows/Super y escribe 'Biblioteca'"
echo "  2. O escribe en cualquier terminal: biblioteca-personal"
echo "=========================================================="
