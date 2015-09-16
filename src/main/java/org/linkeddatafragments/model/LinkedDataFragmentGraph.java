package org.linkeddatafragments.model;

import com.google.common.primitives.Ints;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.graph.impl.GraphMatcher;
import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.shared.AddDeniedException;
import com.hp.hpl.jena.shared.ClosedException;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.WrappedIterator;

import org.linkeddatafragments.client.LinkedDataFragmentsClient;
import org.linkeddatafragments.solver.LDFStatistics;
import org.linkeddatafragments.solver.LinkedDataFragmentEngine;
import org.linkeddatafragments.solver.OpExecutorLDF;
import org.linkeddatafragments.solver.ReorderTransformationLDF;

public class LinkedDataFragmentGraph extends GraphBase {
    protected final LinkedDataFragmentsClient ldfClient;
    protected ReorderTransformation reorderTransform;
    protected LDFStatistics ldfStatistics;

    static {
        // Register OpExecutor
        QC.setFactory(ARQ.getContext(), OpExecutorLDF.opExecFactoryLDF);
        LinkedDataFragmentEngine.register();
    }

    public LinkedDataFragmentGraph(String dataSource) {
          super();
          this.ldfClient = new LinkedDataFragmentsClient(dataSource);
          this.reorderTransform=new ReorderTransformationLDF(this);
          this.ldfStatistics = new LDFStatistics(this);  //must go after ldfClient created
    }

    /**
     Default implementation answers <code>true</code> iff this graph is the
     same graph as the argument graph.
     */
    @Override
    public boolean dependsOn( Graph other )
    { return ldfClient.getBaseFragment().getGraph() == other; }

    @Override
    public void add(Triple t) throws AddDeniedException {
        throw new UnsupportedOperationException();
    }

    /**
     Answer the capabilities of this graph; the default is an AllCapabilities object
     (the same one each time, not that it matters - Capabilities should be
     immutable).
     */
    @Override
    public Capabilities getCapabilities()
    {
        if (capabilities == null) capabilities = new LinkedDataFragmentGraphCapabilities();
        return capabilities;
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(TripleMatch m) {
        try{
            LinkedDataFragment ldf = ldfClient.getFragment(ldfClient.getBaseFragment(), m);
//            ExtendedIterator<Triple> triples = ldf.getTriples();
//            Iterator<LinkedDataFragment> ldfIterator = LinkedDataFragmentIterator.create(ldf, ldfClient);
//            while(ldfIterator.hasNext()) {
//                ldf = ldfIterator.next();
//                triples = triples.andThen(ldf.getTriples());
//            }
            ExtendedIterator<Triple> triples = ExtendedTripleIteratorLDF.create(ldfClient, ldf);
            return triples;
        } catch(Exception e) {
            e.printStackTrace();
            return WrappedIterator.emptyIterator(); //Do not block on error but return empty iterator
        }
    }

    public Long getCount(TripleMatch m) {
        //System.out.println("count requested");
        try{
            LinkedDataFragment ldf = ldfClient.getFragment(ldfClient.getBaseFragment(), m);
            Long count = ldf.getMatchCount();
            //System.out.println(String.format("%s found", count));
            return count;
        } catch(Exception e) {
            return 0L;
        }
    }

    public ReorderTransformation getReorderTransform() {
        return reorderTransform;
    }

    protected int graphBaseSize() {
        try{
            return Ints.checkedCast(ldfClient.getBaseFragment().getTriplesSize());
        } catch(IllegalArgumentException e) {
            return Integer.MAX_VALUE; //return a very high number
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public GraphStatisticsHandler getStatisticsHandler() {
        if(this.ldfStatistics == null) {
            this.ldfStatistics = new LDFStatistics(this);
        }
        return this.ldfStatistics;
    }

    @Override
    public void remove(Node s, Node p, Node o) {
        throw new UnsupportedOperationException();
    }

    /**
     Answer true iff this graph is isomorphic to <code>g</code> according to
     the algorithm (indeed, method) in <code>GraphMatcher</code>.
     */
    @Override
    public boolean isIsomorphicWith( Graph g )
    { checkOpen();
        return g != null && GraphMatcher.equals(ldfClient.getBaseFragment().getGraph(), g); }

    /**
     Answer a human-consumable representation of this graph. Not advised for
     big graphs, as it generates a big string: intended for debugging purposes.
     */

    /**
     Utility method: throw a ClosedException if this graph has been closed.
     */
    protected void checkOpen()
    { if (closed) throw new ClosedException( "already closed", ldfClient.getBaseFragment().getGraph() ); }

    @Override public String toString()
    { return toString( (closed ? "closed " : ""), ldfClient.getBaseFragment().getGraph() ); }

    /**
     Answer a human-consumable representation of <code>that</code>. The
     string <code>prefix</code> will appear near the beginning of the string. Nodes
     may be prefix-compressed using <code>that</code>'s prefix-mapping. This
     default implementation will display all the triples exposed by the graph (ie
     including reification triples if it is Standard).
     */
    public static String toString( String prefix, Graph that )
    {
        PrefixMapping pm = that.getPrefixMapping();
        StringBuffer b = new StringBuffer( prefix + " {" );
        int count = 0;
        String gap = "";
        ClosableIterator<Triple> it = GraphUtil.findAll(that);
        while (it.hasNext() && count < TOSTRING_TRIPLE_LIMIT)
        {
            b.append( gap );
            gap = "; ";
            count += 1;
            b.append( it.next().toString( pm ) );
        }
        if (it.hasNext()) b.append( "..." );
        it.close();
        b.append( "}" );
        return b.toString();
    }

    @Override
    protected ExtendedIterator<Triple> graphBaseFind(Triple triple) {
        return find(triple);
    }

}
