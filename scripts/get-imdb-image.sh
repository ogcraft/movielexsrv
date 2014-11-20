#!/bin/bash
#set -x
url=${1%/}
image_fname=${url##*/}.jpg
echo "Downloading from $1 -> $image_fname"
image_url=`curl -s ${url}/ | grep image_src | grep -o -E 'href="([^"#]+)"' | cut -d'"' -f2`
curl -L -o $image_fname $image_url

exit 0;
