package org.linkeddatafragments.model;


import com.github.fge.uritemplate.URITemplate;
import com.github.fge.uritemplate.URITemplateException;
import com.github.fge.uritemplate.vars.VariableMap;
import com.github.fge.uritemplate.vars.VariableMapBuilder;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.function.Predicate;

public class LinkedDataFragment {
    public static final Resource RDF_PREDICATE = ResourceFactory.createResource("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate");
    protected ExtendedIterator<Triple> triples;
    protected final Model tripleModel;
    protected Long matchCount;
    protected String url;
    protected String nextUrl;
    protected RDFNode fragmentNode;
    protected RDFNode pattern;
    protected String subjectVariable;
    protected String predicateVariable;
    protected String objectVariable;
    protected String template;
    protected URITemplate uriTemplate;
    protected final Triple tripleMatch;

    public LinkedDataFragment(Triple m) {
        this.tripleMatch = m;
        Graph g = GraphFactory.createJenaDefaultGraph();
        g.add(m);
        this.tripleModel = ModelFactory.createModelForGraph(g);
        this.matchCount = 1L;
        this.triples = GraphUtil.findAll(g);
        //hydraParse(g);
    }

    public LinkedDataFragment(Triple m, Long matchCount) {
        this.tripleMatch = m;
        Graph g = GraphFactory.createJenaDefaultGraph();
        if(matchCount > 0) {
            g.add(m);
        }
        this.tripleModel = ModelFactory.createModelForGraph(g);
        this.matchCount = matchCount;
        this.triples = GraphUtil.findAll(g);
        //hydraParse(g);
    }

    public LinkedDataFragment(ExtendedIterator<Triple> triples, Triple m) {
        this.tripleMatch = m;
        Long matchCount = 0L;
        ExtendedIterator<Triple> iteratorTriples = triples;
        Graph g = GraphFactory.createJenaDefaultGraph();
        while (iteratorTriples.hasNext()) {
            matchCount += 1;
            g.add(iteratorTriples.next());
        }
        this.tripleModel = ModelFactory.createModelForGraph(g);
        this.matchCount = matchCount;
        hydraParse(g);
    }

    public LinkedDataFragment(Iterator<Triple> triples, Long matchCount, Triple m) {
        this.tripleMatch = m;
        Graph g = GraphFactory.createJenaDefaultGraph();
        GraphUtil.add(g, triples);
        this.tripleModel = ModelFactory.createModelForGraph(g);
        this.matchCount = matchCount;
        hydraParse(g);
    }

    protected void hydraParse(Graph g) {
        setUrl();
        setNextUrl();
        setFragmentUrl();
        setPattern();
        setTemplate();
        setMatchCount();
        this.triples = GraphUtil.findAll(g)
                .filterKeep(triple -> triple.matches(tripleMatch))
                .filterDrop(triple -> triple.matches(new Triple(NodeFactory.createURI(url), Node.ANY, Node.ANY)))
                .filterDrop(triple -> triple.matches(new Triple(pattern.asNode(), Node.ANY, Node.ANY)))
                .filterDrop(triple -> triple.matches(LinkedDataFragmentsConstants.HYDRA_VARIABLE))
                .filterDrop(triple -> triple.matches(LinkedDataFragmentsConstants.HYDRA_PROPERTY));
        if(fragmentNode != null) {
            this.triples = this.triples.filterDrop(triple -> triple.matches(new Triple(fragmentNode.asNode(), Node.ANY, Node.ANY)));
        }
    }

    public ExtendedIterator<Triple> getTriples() {
        return this.triples;
    }

    public Long getMatchCount() {
        return this.matchCount;
    }

    public String getUrl() {
        return url;
    }

    public Triple getTripleMatch() {
        return this.tripleMatch;
    }

    protected void setUrl() {
        if(Strings.isNullOrEmpty(url)) {
            ResIterator it = this.tripleModel.listSubjectsWithProperty(LinkedDataFragmentsConstants.RDF_TYPE_RESOURCE, LinkedDataFragmentsConstants.VOID_DATASET_RESOURCE);
            while (it.hasNext()) {
                url = it.next().getURI();
                //url = fullUrl.substring(0, fullUrl.indexOf('#'));
            }
        }
    }

    protected void setNextUrl() {
        if(Strings.isNullOrEmpty(nextUrl)) {
            NodeIterator it = this.tripleModel.listObjectsOfProperty(LinkedDataFragmentsConstants.HYDRA_NEXTPAGE);
            while (it.hasNext()) {
                nextUrl = it.next().asResource().getURI();
            }
        }
    }

    protected void setTemplate() {
        NodeIterator it = this.tripleModel.listObjectsOfProperty(LinkedDataFragmentsConstants.HYDRA_TEMPLATE);
        while (it.hasNext()) {
            template = it.next().toString();
        }
        // Throws URITemplateParseException if the template is invalid
        try{
            uriTemplate = new URITemplate(template);
        } catch (URITemplateException e) {
            e.printStackTrace();
        }
    }

    protected void setMatchCount() {
        NodeIterator it = this.tripleModel.listObjectsOfProperty(LinkedDataFragmentsConstants.HYDRA_TOTALITEMS);
        while (it.hasNext()) {
            Literal matchCountLiteral = it.next().asLiteral();
            if(StringUtils.isNumeric(matchCountLiteral.getString())) {
                matchCount = matchCountLiteral.getLong();
            }
        }
    }

    protected void setFragmentUrl() {
        if(fragmentNode == null) {
            ResIterator it = this.tripleModel.listSubjectsWithProperty(LinkedDataFragmentsConstants.RDF_TYPE_RESOURCE, LinkedDataFragmentsConstants.HYDRA_PAGEDCOLLECTION);
            while (it.hasNext()) {
                fragmentNode = it.next();
            }
        }
    }

    public Long getTriplesSize() {
        NodeIterator it = this.tripleModel.listObjectsOfProperty(LinkedDataFragmentsConstants.VOID_TRIPLES);
        Long triplesNumber = 0L;
        while(it.hasNext()) {
            RDFNode number = it.next();
            triplesNumber = number.asLiteral().getLong();
        }
        return triplesNumber;
    }

    public String getUrlToFragment(Triple m) {
        NodeIterator it = getNodePatternIterator();
        while(it.hasNext()) {
            RDFNode mapping = it.next();
            NodeIterator var = this.tripleModel.listObjectsOfProperty(mapping.asResource(), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#property"));
            while(var.hasNext()) {
                Resource typeOfMapping = var.next().asResource();
                if(Strings.isNullOrEmpty(subjectVariable)) {
                if(typeOfMapping.equals(LinkedDataFragmentsConstants.RDF_SUBJECT)) {
                    NodeIterator label = this.tripleModel.listObjectsOfProperty(mapping.asResource(), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#variable"));
                    while(label.hasNext()) {
                        String labelString = label.next().asLiteral().getString();
                        subjectVariable = labelString;
                    }
                }
                }
                if(Strings.isNullOrEmpty(predicateVariable)) {
                if(typeOfMapping.equals(RDF_PREDICATE)) {
                    NodeIterator label = this.tripleModel.listObjectsOfProperty(mapping.asResource(), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#variable"));
                    while(label.hasNext()) {
                        String labelString = label.next().asLiteral().getString();
                        predicateVariable = labelString;
                    }
                }
                }
                if(Strings.isNullOrEmpty(objectVariable)) {
                if(typeOfMapping.equals(LinkedDataFragmentsConstants.RDF_OBJECT)) {
                    NodeIterator label = this.tripleModel.listObjectsOfProperty(mapping.asResource(), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#variable"));
                    while(label.hasNext()) {
                        String labelString = label.next().asLiteral().getString();
                        objectVariable = labelString;
                    }
                }
                }
            }
        }
        // Create a variable map, consisting of name/value pairs
        final VariableMapBuilder builder = VariableMap.newBuilder();

        if(m.getMatchSubject() != null &&  m.getMatchSubject().isConcrete()) {
            if(m.getMatchSubject().isURI()) {
                builder.addScalarValue(subjectVariable, m.getMatchSubject().getURI());
            } else {
                builder.addScalarValue(subjectVariable, String.format("\"%s\"@%s", m.getMatchSubject().getLiteralLexicalForm(), m.getMatchSubject().getLiteralLanguage()));
            }
        }
        if(m.getMatchPredicate() != null && m.getMatchPredicate().isConcrete()) {
            builder.addScalarValue(predicateVariable, m.getMatchPredicate().getURI());
        }
        if(m.getMatchObject() != null && m.getMatchObject().isConcrete()) {
            if(m.getMatchObject().isURI()) {
                builder.addScalarValue(objectVariable, m.getMatchObject().getURI());
            } else {
                builder.addScalarValue(objectVariable, String.format("\"%s\"@%s",m.getMatchObject().getLiteralLexicalForm(),m.getMatchObject().getLiteralLanguage()));
                //builder.addScalarValue(objectVariable, String.format("%s",m.getMatchObject().getLiteral()));

            }
        }

        // Create the variable map
        final VariableMap vars = builder.freeze();

        try {

            return uriTemplate.toURL(vars).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected NodeIterator getNodePatternIterator() {

        setPattern();
        NodeIterator it;

        if(!(pattern == null)) {
            it = this.tripleModel.listObjectsOfProperty(pattern.asResource(), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#mapping"));
        } else {
            it = this.tripleModel.listObjectsOfProperty(ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#mapping"));
        }
        return it;
    }

    protected void setPattern() {
        NodeIterator it = this.tripleModel.listObjectsOfProperty(ResourceFactory.createResource(url), ResourceFactory.createProperty("http://www.w3.org/ns/hydra/core#search"));
        while(it.hasNext()) {
            pattern = it.next();
        }
    }

    public Graph getGraph() {
        Graph g = GraphFactory.createJenaDefaultGraph();
        GraphUtil.add(g, triples);
        return g;
    }

    public String getNextUrl() {
        return nextUrl;
    }

    public Boolean hasNextUrl() {
        return !Strings.isNullOrEmpty(nextUrl);
    }
}
