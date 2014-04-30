package org.linkeddatafragments.model;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatchFilter;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Created by ldevocht on 4/29/14.
 */
public class LinkedDataFragmentsConstants {
    public static final TripleMatchFilter HYDRA_VARIABLE = new TripleMatchFilter(new Triple(Node.ANY, NodeFactory.createURI("http://www.w3.org/ns/hydra/core#variable"), Node.ANY));
    public static final TripleMatchFilter HYDRA_PROPERTY = new TripleMatchFilter(new Triple(Node.ANY, NodeFactory.createURI("http://www.w3.org/ns/hydra/core#property"), Node.ANY));
    public static final Property RDF_TYPE_RESOURCE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    public static final Resource VOID_DATASET_RESOURCE = ResourceFactory.createResource("http://rdfs.org/ns/void#Dataset");
    public static final Property HYDRA_NEXTPAGE = ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#nextPage");
    public static final Property HYDRA_TOTALITEMS = ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#totalItems");
    public static final Property HYDRA_TEMPLATE = ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#template");
    public static final Resource HYDRA_PAGEDCOLLECTION = ResourceFactory.createResource("http://www.w3.org/ns/hydra/core#PagedCollection");
    public static final Property VOID_TRIPLES = ResourceFactory.createProperty("http://rdfs.org/ns/void#triples");
    public static final Resource RDF_SUBJECT = ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
    public static final Resource RDF_OBJECT = ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
}
