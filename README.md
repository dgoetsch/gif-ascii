# Gif to ascii

Downloads arbitrary gifs from a url, transforms them to ascii, saves them in a db.

### Future Plans

* Integrate with elastic search for tagging + naming
* Gif Speed Configuration (probabaly UI only, could store configuration in db)
* Tests (next on list)

### Run the application

1. publish docker image for service `sbt docker:publishLocal`
2. run docker-compose: `docker-compose up`
3. browser to image container on port 9000 (for me its always something lke `http://172.19.0.3:9000/`)
