# Grocery Store Advisor
A little advisor for searching where you can buy the cheapest household supply products.

The deployed app can be found here:

[http://грошри.рф](http://грошри.рф)

The API can be found here:

[http://грошри.рф/api](http://грошри.рф/api)

or here:

[http://грошри.рф:8080](http://грошри.рф:8080)

Example of an API call: [http://грошри.рф:8080/commodities?q=Шоколад](http://грошри.рф:8080/commodities?q=Шоколад)

## Modules
### *Extractor*
The *Extractor* module extracts data from grocery web sites using [web scraping](http://en.wikipedia.org/wiki/Web_scraping).

Currently it is able to send extracted entries to [Akka Remote System](http://doc.akka.io/docs/akka/snapshot/scala/remoting.html) or standard output.

For uploading data to the akka remote system actor located at "akka.tcp://WhereToBuySystem@127.0.0.1:9000/user/EntryExtractingActor":
```
sbt "run akka"
```

Alternatively you can specify the system:
```
WH_API_AKKA_ENDPOINT="akka.tcp://WhereToBuySystem@127.0.0.1:9000/user/EntryExtractingActor" sbt "run akka"
```

Or simply to console
```
sbt "run console"
```

### *API*
The *API* module is implemented as a RESTful HTTP JSON API, which is used by the *Web App* module.

Also the module implements the logic of matching entries from several shops into one *Commodity* domain object.
For that the module uses Neural Network implemented by [Weka](http://www.cs.waikato.ac.nz/ml/weka/).

The module uses [Spray](http://spray.io) for the RESTful API.

### *Web App*
It consists of two parts: *proxy* that forwards all requests from "/api" to the API module and *simple static files*.

Basicly, it is a web 2.0 app that contains almost no backend logic (except the logic described above).

The web 2.0 app is mostly based on AngularJS and Bootstrap.

## Technologies, Patterns and Philosophy
### *Domain Driven Development*
The architecture of the code is inspired by DDD, mostly by [Vaughn Vernon] (https://vaughnvernon.co).

Domain Models are clearly separated and integrated at the Application Layer.

Examples:
 * *Inventory Domain Model* models such objects as *Commodity* and *Shop*. Needed for storing data, keeping track of prices and so on. See "wh.inventory.domain.model" package.
 * *Image Domain Model* models a sub domain for working with images. Needed for storing and downloading images of commodities. Note that the domain is separated from the previous one, since working with images is not a domain operation of the *Inventory Domain*.

### *Functional Programming*
Almost everything is immutable. Yup, every domain object is immutable.

### *Event Sourcing*
Every domain object manage a stream of event on every change. Then repositories thransparently store the events.

### *Command and Query Responsibility Segregation*
Since Event Store is not very good for complex queries, repositories store snapshots of objects specially for querying.

## Building and Deployment
The app is designed to be deployed to [Coreos](https://coreos.com) cluster.

Building happens directly on [Docker Hub](https://hub.docker.com) on push to the *master* branch.

After the build is done, the Docker images are automatically pulled by a special service on prod.
