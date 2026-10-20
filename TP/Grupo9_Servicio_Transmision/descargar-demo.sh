#!/bin/bash
# Baja el recital de demostracion a la carpeta media/ del servicio.
#
# "Sunrust | Live Concert" (Port Orchard Concerts by the Bay), de archive.org,
# dedicado al dominio publico (CC0): 114 minutos, 854x480, 680 MB.
# https://archive.org/details/sunrust-2023-08-17
#
# El evento «Sunrust - Transmision permanente» de los datos de prueba usa este video.

cd "$(dirname "$0")" || exit 1
mkdir -p media

if [ -f media/sunrust.mp4 ]; then
    echo "media/sunrust.mp4 ya existe."
    exit 0
fi

curl -L -C - --progress-bar -o media/sunrust.mp4 \
    "https://archive.org/download/sunrust-2023-08-17/sunrust.mp4" && echo "Listo: media/sunrust.mp4"
