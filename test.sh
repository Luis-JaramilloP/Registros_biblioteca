#!/usr/bin/env bash
set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

JAVAFX_LIB="/usr/lib/jvm/openjfx"
TARGET_DIR="$PROJECT_DIR/target"
CLASSES_DIR="$TARGET_DIR/classes"
TEST_CLASSES_DIR="$TARGET_DIR/test-classes"

# Asegurar compilación previa
if [ ! -d "$CLASSES_DIR" ]; then
    ./build.sh
fi

mkdir -p "$TEST_CLASSES_DIR"

echo "=========================================================="
echo "  COMPILANDO Y EJECUTANDO PRUEBAS UNITARIAS/INTEGRACIÓN"
echo "=========================================================="

TEST_SOURCES=$(find src/test/java -name "*.java")

javac --release 17 \
      --module-path "$JAVAFX_LIB" \
      --add-modules javafx.controls,javafx.fxml \
      -cp "$CLASSES_DIR" \
      -d "$TEST_CLASSES_DIR" \
      $TEST_SOURCES

java --module-path "$JAVAFX_LIB" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "$CLASSES_DIR:$TEST_CLASSES_DIR" \
     com.biblioteca.AppTestSuite

echo "=========================================================="
echo "  ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!"
echo "=========================================================="
