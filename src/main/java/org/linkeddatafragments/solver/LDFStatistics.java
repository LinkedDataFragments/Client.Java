package org.linkeddatafragments.solver;

import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;


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
    @Override
    public long getStatistic(Node subject, Node predicate, Node object) {
        //System.out.println("statistics requested");
        return ldfG.getCount(Triple.createMatch(subject, predicate, object));
    }

}