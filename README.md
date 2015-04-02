# Riemann Druid Emitter
[Riemann](http://riemann.io) event emitter for [Druid](https://github.com/druid-io/).

## Installation
Get source code
```
git clone https://github.com/msprunck/riemann-emitter.git
```
Build the jar file:
```
cd riemann-emitter
mvn clean install
```
  This will create the "target" folder containing the ```riemann-emitter-{version}.jar``` file. Add it to the classpath of Druid.

Artifacts are available through
[clojars](https://clojars.org/com.sprunck.druid/riemann-emitter). See below.


## Configuration
Add the following properties to the [Druid configuration](http://druid.io/docs/latest/Configuration.html).

Property|Value|Description
--- | --- | ---
druid.emitter|riemann|Enable the Riemann emitter
druid.emitter.riemann.port||The riemann server port
druid.emitter.riemann.host||The riemann host
druid.extensions.remoteRepositories|["http://repo1.maven.org/maven2/", "http://clojars.org/repo"]|JSON Array list of remote repositories to load dependencies from. 
druid.extensions.coordinates|["com.sprunck.druid:riemann-emitter:0.1.0-beta"]|JSON array of maven coordinates.



## Riemann event

The Riemann service attribute is composed by the druid service name and the metric name.

Druid service|Druid Metric|Riemann service
--- | --- | ---
druid/historical|cache/total/hits|druid/historical cache/total/hits