package org.linkeddatafragments.solver;

import org.apache.jena.graph.GraphStatisticsHandler;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.engine.optimizer.Pattern;
import org.apache.jena.sparql.engine.optimizer.StatsMatcher;
import org.apache.jena.sparql.engine.optimizer.reorder.PatternTriple;
import org.apache.jena.sparql.engine.optimizer.reorder.ReorderTransformationSubstitution;
import org.apache.jena.sparql.graph.NodeConst;
import org.apache.jena.sparql.sse.Item;
import org.linkeddatafragments.model.LinkedDataFragmentGraph;

import static org.apache.jena.sparql.engine.optimizer.reorder.PatternElements.TERM;
import static org.apache.jena.sparql.engine.optimizer.reorder.PatternElements.VAR;


/**
 * Reorders the Triple Patterns of a BGP by using statistics directly fetched from
 * the dataset. At query optimization phase, when planning joins, some variables
 * are known to be bound at some stage, but the actual values are unknown.
 * In this case it uses predefined typical behaviour for RDF, using independent
 * histograms for S/P/O, inspired by Jena's FixedReorder and HDT's reorder.
 *
 * @author ldevocht
 *
 */
public class ReorderTransformationLDF extends ReorderTransformationSubstitution {

    /** Maximum value for a match involving two terms. */
    public final long multiTermMax ;

    private final long numTriples ;         // Actual number of triples of the dataset.

    private GraphStatisticsHandler stats;
    public final StatsMatcher matcher = new StatsMatcher() ;


    public ReorderTransformationLDF(LinkedDataFragmentGraph graph)
    {
        this.stats = graph.getStatisticsHandler();
        numTriples = graph.size();
        multiTermMax = numTriples/100;
        initializeMatcher();

    }

    private void initializeMatcher() {
        Item type = Item.createNode(NodeConst.nodeRDFType);

        //matcher.addPattern(new Pattern(1,   TERM, TERM, TERM)) ;     // SPO - built-in - not needed as a rule

        // Numbers choosen as an approximation for a ldf graph of size numTriples
        matcher.addPattern(new Pattern(                                 5,  TERM,   TERM,  VAR));     // SP?
        matcher.addPattern(new Pattern(Math.max(numTriples /  1000, 1000),   VAR,   type,  TERM));    // ? type O -- worse than ?PO
        matcher.addPattern(new Pattern(Math.max(numTriples / 10000,   90),   VAR,   TERM,  TERM));    // ?PO
        matcher.addPattern(new Pattern(                                 3,  TERM,    VAR,  TERM));    // S?O
        matcher.addPattern(new Pattern(                                40,  TERM,    VAR,  VAR));     // S??
        matcher.addPattern(new Pattern(                               200,   VAR,    VAR,  TERM));    // ??O
        matcher.addPattern(new Pattern(Math.max(numTriples / 200,   2000),   VAR,   TERM,  VAR));     // ?P?

        matcher.addPattern(new Pattern(numTriples, VAR, VAR, VAR));     // ???
    }

    @Override
    protected double weight(PatternTriple pt)
    {
        // If all are nodes, there are no substitutions. We can get the exact number.
        if(pt.subject.isNode() && pt.predicate.isNode() && pt.object.isNode()) {
            return stats.getStatistic(pt.subject.getNode(), pt.predicate.getNode(), pt.object.getNode());
        }

        // Try on fixed
        double x = matcher.match(pt);

        // If there are two fixed terms, use the fixed weighting, all of which are quite small.
        // This chooses a less optimal triple but the worse choice is still a very selective choice.
        // One case is IFPs: the multi term choice for PO is not 1.

        if ( x < multiTermMax )
        {
            return x;
        }

        // One or zero fixed terms.
        // Otherwise, assuming S / P / O independent, do an estimation.

        long S = stats.getStatistic(pt.subject.getNode(), Node.ANY, Node.ANY);
        long P = stats.getStatistic(Node.ANY, pt.predicate.getNode(), Node.ANY);
        long O = stats.getStatistic(Node.ANY, Node.ANY, pt.object.getNode());

        if ( S == 0 || P == 0 || O == 0 ) {
            // Can't match.
            return 0 ;
        }

        // Find min positive
        x = -1 ;
        if ( S > 0 ) x = S ;
        if ( P > 0 && P < x ) x = P ;
        if ( O > 0 && O < x ) x = O ;
        //System.out.printf("** [%d, %d, %d]\n", S, P ,O) ;

        return x;
    }
}