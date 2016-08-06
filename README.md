#Gif to ascii
Downloads arbitrary gifs from a url, transforms them to ascii, saves them in a db.

In the future, this will use elastic search to let users find gifs. And there will be tests.

To run:

1. publish docker image for service `sbt docker:publishLocal`
2. run docker-compose: `docker-compose up`
3. browser to image container on port 9000 (for me its always something lke `http://172.19.0.3:9000/`)
