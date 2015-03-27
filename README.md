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


## Configuration
Add the following properties to the [Druid configuration](http://druid.io/docs/latest/Configuration.html).

Property|Value|Description
--- | --- | ---
druid.emitter|riemann|Enable the Riemann emitter
druid.emitter.riemann.port||The riemann server port
druid.emitter.riemann.host||The riemann host

## Riemann event

The Riemann service attribute is composed by the druid service name and the metric name.

Druid service|Druid Metric|Riemann service
--- | --- | ---
druid/historical|cache/total/hits|druid/historical cache/total/hits