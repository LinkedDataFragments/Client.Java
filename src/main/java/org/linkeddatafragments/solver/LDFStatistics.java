package org.linkeddatafragments.solver;

import org.linkeddatafragments.model.LinkedDataFragmentGraph;

import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * @author ldevocht
 *
 */
public class LDFStatistics implements GraphStatisticsHandler {

    private final LinkedDataFragmentGraph ldfG;

    /**
     *
     */
    public LDFStatistics(LinkedDataFragmentGraph graph) {
        this.ldfG = graph;
    }

    /* (non-Javadoc)
     * @see com.hp.hpl.jena.graph.GraphStatisticsHandler#getStatistic(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
     */
    public long getStatistic(Node subject, Node predicate, Node object) {
        //System.out.println("statistics requested");
        return ldfG.getCount(Triple.createMatch(subject, predicate, object));
    }

}