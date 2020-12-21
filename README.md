# LOCERS
## LOCERS: A Lightweight Ontology Cache for Expressive (OWL2) Reasoning over Streams

LOCERS is a lightweight cache for expressive OWL2 reasoning over streams of events. 
It exploits the characteristics of the stream in order to prevent reoccuring reasoning steps. 
More specifically it exploits the fact that event streams typically contain the same structure and size of events.

## Usage
LOCERS can be used in the following way:
```
OWLOntology ontology = ... //OWL API Ontology reference
LOCERSMaterializeCache cache = new LOCERSMaterializeCache();
cache.init(ontology);
cache.setCacheStructure(new MaterializeCacheStructure());
Set<OWLAxiom> event = ... //Event as OWL API axioms
Set<OWLAxiom> results = cache.check(event);
```
The results set contains the materialized event.

## Building LOCERS
You can build LOCERS using gradle:
```
gradle
gradle wrapper
```
To execute one of the examples in an executable jar:
```
./gradlew shadowJar
 mv build/libs/LOCERS-0.0.1-all.jar locers.jar
java -jar locers.jar resources
```
In order to change the example, change OWL2BenchTest to CityBenchPlusTest on line 13 of the build.gradle file. 