# Linked Data Fragments Client <img src="http://linkeddatafragments.org/images/logo.svg" width="100" align="right" alt="" />

**This is a prototype implementation of a Linked Data Fragments client for the Jena stack.
The recommended client is the [JavaScript implementation](https://github.com/LinkedDataFragments/Client.js/).**

Efficient execution of SPARQL queries against Triple Pattern Fragment interfaces
relies on parallelization, which Jena does not seem suited for.
As such, consider this code as an example, not as a recommended implementation.

## Usage

This adapter lets you use [Triple Pattern Fragments](http://linkeddatafragments.org/in-depth/#tpf) in Jena.

For example, use as a Jena Model:

```Java
LinkedDataFragmentGraph ldfg = new LinkedDataFragmentGraph("http://data.linkeddatafragments.org/dbpedia");
Model model = ModelFactory.createModelForGraph(ldfg);
```

Example queries:

```SPARQL
SELECT ?p ?o WHERE { <http://dbpedia.org/resource/Barack_Obama> ?p ?o }

SELECT ?o ?n WHERE { <http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/almaMater> ?o . ?o <http://dbpedia.org/ontology/state> ?n }
```

## Status
Technical preview only.
In production, use the [JavaScript client](https://github.com/LinkedDataFragments/Client.js).
