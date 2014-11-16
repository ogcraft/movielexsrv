#!/bin/bash
#set -x
url=${1%/}
image_fname=${url##*/}.jpg
echo "Downloading from $1 -> $image_fname"
image_url="http://www.kinopoisk.ru/images/film_big/${image_fname}"
curl -L -o $image_fname $image_url

exit 0;
