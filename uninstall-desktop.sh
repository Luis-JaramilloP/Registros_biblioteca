#!/usr/bin/env bash
set -e

APPS_DIR="$HOME/.local/share/applications"
ICONS_DIR="$HOME/.local/share/icons/hicolor/scalable/apps"
BIN_DIR="$HOME/.local/bin"

echo "Desinstalando lanzador de Biblioteca Personal..."

rm -f "$APPS_DIR/biblioteca-personal.desktop"
rm -f "$ICONS_DIR/biblioteca-personal.svg"
rm -f "$BIN_DIR/biblioteca-personal"

if command -v update-desktop-database >/dev/null 2>&1; then
    update-desktop-database "$APPS_DIR" >/dev/null 2>&1 || true
fi

echo "¡Lanzador desinstalado del sistema!"
