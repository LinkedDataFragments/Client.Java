package be.mmlab.ldfjena;

/**
 * Created by ldevocht on 4/28/14.
 */

import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import java.io.IOException;

import be.mmlab.ldfjena.model.LinkedDataFragmentGraph;
import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.AssemblerBase;
import com.hp.hpl.jena.assembler.exceptions.AssemblerException;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

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