package org.linkeddatafragments.client;

import com.google.common.base.Optional;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.TriplePattern;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.linkeddatafragments.model.LinkedDataFragment;
import org.linkeddatafragments.model.LinkedDataFragmentFactory;
import org.linkeddatafragments.model.LinkedDataFragmentIterator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

public class LinkedDataFragmentsClient {
    protected Graph tripleModel;  //TODO: use this model to check ground patterns without having to actually fetch a fragment if once had a 200 OK then fine and temp store here.
    protected final String dataSource;
    protected final CloseableHttpAsyncClient httpAsyncClient = HttpAsyncClients.createDefault();
    protected final Cache<String, LinkedDataFragment> fragments = CacheBuilder.newBuilder()
            .maximumSize(10000) //Maximum caching size
            .build();
    //protected Optional<String> entryFragment;

    public LinkedDataFragmentsClient(String dataSource) {
        this.dataSource = dataSource;
        //this.tripleModel = ModelFactory.createDefaultModel() //temporary model for found (ground) patterns.
    }

    protected void addTriples(Iterator<Triple> triples) {
        while (triples.hasNext()) {
            Triple t = triples.next();
            tripleModel.add(t);
        }
        // they are not pushed back to the server - only kept on client
    }

    public LinkedDataFragment getBaseFragment() {
        Model fragmentTriples = ModelFactory.createDefaultModel();
        fragmentTriples.read(this.dataSource, "TURTLE");
        //GraphUtil.addInto(tripleModel,fragmentTriples.getGraph());
        return LinkedDataFragmentFactory.create(GraphUtil.findAll(fragmentTriples.getGraph()), fragmentTriples.size(), Triple.ANY);
    }


    public LinkedDataFragment getFragment(LinkedDataFragment baseFragment, TripleMatch pattern) throws Exception {
        //Boolean hasVariables;
        //Node s,p,o;
        String method = "GET";
        TripleMatch tripleTemplate = pattern;
        TriplePattern p = new TriplePattern(pattern);

        // TODO: if looking for a specific triple (no variables), we can use shortcuts
        if (p.isGround()) {
            //if (tripleModel.contains(pattern.asTriple())) { //check if we already asked for a ground pattern
            //    Graph g = GraphFactory.createJenaDefaultGraph();
            //    g.add(pattern.asTriple());
            //    return LinkedDataFragmentFactory.create(GraphUtil.findAll(g), 1L, pattern.asTriple()); // do not send request if pattern was already fetched
            //} else {
                method = "HEAD"; //only check if there is a 200 OK, don't actually read data
            //}

        }

        // follow the given fragment to retrieve the requested fragment
        String fragmentUrl = baseFragment.getUrlToFragment(tripleTemplate);
        //System.out.println(fragmentUrl);
        LinkedDataFragment ldf = getFragment(method, fragmentUrl, tripleTemplate);

        return ldf;
    }
    // ?s ?p ?o .
    // Triple patternT =
    //        Triple.create(Var.alloc("s"), Var.alloc("p"), Var.alloc("o"));
    // new TriplePattern(patternT);
    public LinkedDataFragment getFragment(String method, String fragmentUrl, TripleMatch tripleTemplate) throws Exception {
//        String hash = "" + fragmentUrl.hashCode() + tripleTemplate.hashCode();
//
//        Optional<LinkedDataFragment> fragmentOptional = Optional.fromNullable(fragments.getIfPresent(hash));
//        // check the fragment cache
//        if (fragmentOptional.isPresent()) {
//            return fragmentOptional.get();
//        }

        Model fragmentTriples = ModelFactory.createDefaultModel();
        fragmentTriples.getReader().setProperty("WARN_UNQUALIFIED_RDF_ATTRIBUTE","EM_IGNORE");
        fragmentTriples.getReader().setProperty("allowBadURIs","true");

        HttpResponse response = getLinkedDataFragment(method, fragmentUrl);
        //System.out.println(fragmentUrl);
        //String remoteUrl = baseFragment.getUrlToFragment(tripleTemplate);
        //fragmentTriples.read(remoteUrl,"TURTLE");
        LinkedDataFragment ldf;
        if(method == "GET") {
            InputStream in = parseLDFInputStream(response);
            fragmentTriples.read(in, null, "TURTLE");

            ldf = LinkedDataFragmentFactory.create(GraphUtil.findAll(fragmentTriples.getGraph()), fragmentTriples.size(), tripleTemplate);
//        fragments.put(hash, ldf);
        } else {
            if(response.getStatusLine().getStatusCode() == 200) {
                Graph g = GraphFactory.createJenaDefaultGraph();
                g.add(tripleTemplate.asTriple());
                ldf = LinkedDataFragmentFactory.create(tripleTemplate); // do not send request if pattern was already fetched
            } else {
                ldf = LinkedDataFragmentFactory.create(tripleTemplate, 0L);
            }
        }
        return ldf;
    }

    private InputStream parseLDFInputStream(HttpResponse response) throws IOException {
        InputStream in = response.getEntity().getContent();
        Header ceheader = response.getEntity().getContentEncoding();
        if (ceheader != null) {
            HeaderElement[] codecs = ceheader.getElements();
            for (int i = 0; i < codecs.length; i++) {
                if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                    in = new GZIPInputStream(response.getEntity().getContent());
                }
            }
        }
        return in;
    }

    private HttpResponse getLinkedDataFragment(String method, String fragmentUrl) throws InterruptedException, java.util.concurrent.ExecutionException, IOException {
        httpAsyncClient.start();
        HttpRequestBase request;
        if (method.equalsIgnoreCase("GET")) {
            request = new HttpGet(fragmentUrl);
        } else {
            request = new HttpHead(fragmentUrl); //TODO what happens if the head is fetched?
        }
        request.setHeader("Accept", "text/turtle");
        request.setHeader("Accept-Encoding","gzip");
        Future<HttpResponse> future = httpAsyncClient.execute(request, null);
        //TODO return the future instead of synchronizing here...
        HttpResponse response = future.get();
        //httpAsyncClient.close();
        return response;
    }
}
