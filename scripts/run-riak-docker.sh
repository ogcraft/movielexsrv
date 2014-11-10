#!/bin/bash
#docker run -d -t -i -p 8069:8069 -p 8087:8087 -p 8098:8098 -v ${HOME}/movielex_data/riak:/var/lib/riak --name riak1 lapax/riak
docker run -d -t -i -p 8069:8069 -p 8087:8087 -p 8098:8098 --name riak1 lapax/riak
