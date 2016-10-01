# Gif to ascii

Downloads arbitrary gifs from a url, transforms them to ascii, saves them in a db.

### Future Plans

* Integrate with elastic search for tagging + naming
* Gif Speed Configuration (probabaly UI only, could store configuration in db)
* Tests (next on list)

### Required Software

* [sbt](http://www.scala-sbt.org/download.html)
* [Docker](https://docs.docker.com/engine/installation/)

### Run the application

1. publish docker image for service `sbt docker:publishLocal`
2. run docker-compose: `docker-compose up`
3. browser to [http://localhost:9000](http://localhost:9000)

### Endpoints

To see the form:

* `GET     /`
* `GET     /download`

You will be redirected here:

* `GET     /handle-download`
  * This will first check to see if the given gif had been downloaded.  If it had not, then it will download it and pre compute the ascii representation of the gif.
  * parameters:
    * url: escaped url of the gif
    * size:
      * a string with a substring from the two following sets of strings:
        * `"xtra", "supe", "more", "real"`
        * `"larg", "big", "smal", "peq", "lit"`
      * For example `superbig` or `extra_small` or `real little`
  * Example: [http://localhost:9000/handle-download?url=https%3A%2F%2Fmedia.giphy.com%2Fmedia%2FW80Y9y1XwiL84%2Fgiphy.gif&size=SUPERBIG](http://localhost:9000/handle-download?url=https%3A%2F%2Fmedia.giphy.com%2Fmedia%2FW80Y9y1XwiL84%2Fgiphy.gif&size=SUPERBIG)
