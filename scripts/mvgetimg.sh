#!/bin/bash
#set -x
url=${1%/}
image_fname=${url##*/}.jpg

if [[ $url == *imdb.com* ]]
then
	echo "Downloading from $1 -> $image_fname"
	image_url=`curl -s ${url}/ | grep image_src | grep -o -E 'href="([^"#]+)"' | cut -d'"' -f2`
	curl -L -o $image_fname $image_url
fi


if [[ $url == *kinopoisk.ru* ]]
then
	echo "Downloading from $1 -> $image_fname"
	image_url="http://www.kinopoisk.ru/images/film_big/${image_fname}"
	curl -L -o $image_fname $image_url
fi


exit 0;
