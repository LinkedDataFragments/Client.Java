package org.linkeddatafragments.client;

/**
 * Created by ldevocht on 4/28/14.
 */

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.Mode;
import org.apache.jena.assembler.assemblers.AssemblerBase;
import org.apache.jena.assembler.exceptions.AssemblerException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;


public class LinkedDataFragmentsGraphAssembler extends AssemblerBase implements Assembler {

    private static boolean initialized = false;
    private static String JENA_NS = "http://linkeddatafragments.org/fuseki#";

    public static void init() {
        if(initialized) {
            return;
        }

        initialized = true;

        Assembler.general.implementWith(ResourceFactory.createResource(JENA_NS + "LDFGraph"), new LinkedDataFragmentsGraphAssembler());
    }

    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        String url = GraphUtils.getStringValue(root, ResourceFactory.createProperty(JENA_NS + "url")) ;

        try {
            // FIXME: Read more properties. Cache config?
            LinkedDataFragmentGraph graph = new LinkedDataFragmentGraph(url);
            return ModelFactory.createModelForGraph(graph);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AssemblerException(root, "Error reading LDF url: "+url+" / "+e.toString());
        }
    }
}