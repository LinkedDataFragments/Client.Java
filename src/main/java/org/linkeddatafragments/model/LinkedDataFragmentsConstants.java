package org.linkeddatafragments.model;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatchFilter;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Shared constants.
 * 
 * @author ldevocht
 * @author agazzarini
 */
public class LinkedDataFragmentsConstants {
	public static final String HYDRA_NAMESPACE = "http://www.w3.org/ns/hydra/core#";
    public static final TripleMatchFilter HYDRA_VARIABLE = new TripleMatchFilter(new Triple(Node.ANY, NodeFactory.createURI(HYDRA_NAMESPACE + "variable"), Node.ANY));
    public static final TripleMatchFilter HYDRA_PROPERTY = new TripleMatchFilter(new Triple(Node.ANY, NodeFactory.createURI(HYDRA_NAMESPACE + "property"), Node.ANY));
    public static final Property RDF_TYPE_RESOURCE = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
    public static final Resource VOID_DATASET_RESOURCE = ResourceFactory.createResource("http://rdfs.org/ns/void#Dataset");
    public static final Property HYDRA_NEXTPAGE = ResourceFactory.createProperty(HYDRA_NAMESPACE + "nextPage");
    public static final Property HYDRA_TOTALITEMS = ResourceFactory.createProperty(HYDRA_NAMESPACE + "totalItems");
    public static final Property HYDRA_TEMPLATE = ResourceFactory.createProperty(HYDRA_NAMESPACE + "template");
    public static final Resource HYDRA_PAGEDCOLLECTION = ResourceFactory.createResource(HYDRA_NAMESPACE + "PagedCollection");
    public static final Property VOID_TRIPLES = ResourceFactory.createProperty("http://rdfs.org/ns/void#triples");
    public static final Resource RDF_SUBJECT = ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#subject");
    public static final Resource RDF_OBJECT = ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#object");
}
