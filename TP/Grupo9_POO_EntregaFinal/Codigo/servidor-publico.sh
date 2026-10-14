#!/bin/bash
# Abre un tunel de Cloudflare hacia el puerto 8765 y arranca el servidor de
# UADE Beats con la direccion publica que le toca. Ctrl+C cierra las dos cosas.
#
# Uso: ./servidor-publico.sh [completo]
#   (sin argumento)  solo servidor, sin interfaz
#   completo         servidor y ventana o consola local

cd "$(dirname "$0")" || exit 1

PUERTO=8765
REGISTRO=$(mktemp)

# Compila si hace falta.
if [ ! -f out/Main.class ]; then
    javac -encoding UTF-8 -cp "lib/*" -d out $(find src -name "*.java") || exit 1
fi

cloudflared tunnel --no-autoupdate --url "http://localhost:$PUERTO" > "$REGISTRO" 2>&1 &
TUNEL=$!
trap 'kill $TUNEL 2>/dev/null; rm -f "$REGISTRO"' EXIT

echo "Abriendo el tunel..."
URL=""
for _ in $(seq 1 30); do
    URL=$(grep -o "https://[a-z0-9-]*\.trycloudflare\.com" "$REGISTRO" | head -1)
    [ -n "$URL" ] && break
    sleep 1
done

if [ -z "$URL" ]; then
    echo "No se pudo abrir el tunel. Salida de cloudflared:"
    cat "$REGISTRO"
    exit 1
fi

echo
echo "Direccion publica: $URL"
echo "Para conectar un cliente:"
echo "  java -cp \"out:lib/*\" Main cliente $URL"
echo

MODO="servidor"
[ "$1" = "completo" ] && MODO=""

java --enable-native-access=ALL-UNNAMED -Duadebeats.puerto=$PUERTO \
     -Duadebeats.url="$URL" -cp "out:lib/*" Main $MODO
