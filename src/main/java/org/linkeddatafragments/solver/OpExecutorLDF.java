package org.linkeddatafragments.solver;

import org.linkeddatafragments.model.LinkedDataFragmentGraph;

import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpBGP;
import com.hp.hpl.jena.sparql.algebra.op.OpDistinct;
import com.hp.hpl.jena.sparql.algebra.op.OpFilter;
import com.hp.hpl.jena.sparql.algebra.op.OpReduced;
import com.hp.hpl.jena.sparql.algebra.optimize.TransformFilterPlacement;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Substitute;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPeek;
import com.hp.hpl.jena.sparql.engine.main.OpExecutor;
import com.hp.hpl.jena.sparql.engine.main.OpExecutorFactory;
import com.hp.hpl.jena.sparql.engine.main.QC;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderProc;
import com.hp.hpl.jena.sparql.engine.optimizer.reorder.ReorderTransformation;
import com.hp.hpl.jena.sparql.expr.ExprList;

public class OpExecutorLDF extends OpExecutor {

    public final static OpExecutorFactory opExecFactoryLDF = new OpExecutorFactory()
    {
        @Override
        public OpExecutor create(ExecutionContext execCxt)
        {
            return new OpExecutorLDF(execCxt) ;
        }
    };

    private boolean isForLDF;

    protected OpExecutorLDF(ExecutionContext execCtx) {
        super(execCtx);

        isForLDF = (execCtx.getActiveGraph() instanceof LinkedDataFragmentGraph) ;
    }

    @Override
    protected QueryIterator execute(OpDistinct opDistinct, QueryIterator input)
    {
        // FIXME: Add to context duplicates property.
        return super.execute(opDistinct, input) ;
    }

    @Override
    protected QueryIterator execute(OpReduced opReduced, QueryIterator input)
    {
        // FIXME: Add to context duplicates property.
        return super.execute(opReduced, input) ;
    }

    @Override
    protected QueryIterator execute(OpFilter opFilter, QueryIterator input)
    {
        if ( !isForLDF)
            return super.execute(opFilter, input) ;

        // If the filter does not apply to the input??
        // Where does ARQ catch this?

        // (filter (bgp ...))
        if ( OpBGP.isBGP(opFilter.getSubOp()) )
        {
            // Still may be a LDF graph in a non-LDF dataset (e.g. a named model)
            LinkedDataFragmentGraph graph = (LinkedDataFragmentGraph)execCxt.getActiveGraph() ;
            OpBGP opBGP = (OpBGP)opFilter.getSubOp() ;
            return executeBGP(graph, opBGP, input, opFilter.getExprs(), execCxt) ;
        }

        // (filter (anything else))
        return super.execute(opFilter, input) ;
    }

    // ---- Triple patterns

    @Override
    protected QueryIterator execute(OpBGP opBGP, QueryIterator input)
    {
        if ( !isForLDF)
            return super.execute(opBGP, input) ;

        LinkedDataFragmentGraph graph = (LinkedDataFragmentGraph)execCxt.getActiveGraph() ;

        return executeBGP(graph, opBGP, input, null, execCxt);
    }

    /** Execute a BGP (and filters) on a LDF graph, which may be in default storage or it may be a named graph */
    private static QueryIterator executeBGP(LinkedDataFragmentGraph graph, OpBGP opBGP, QueryIterator input, ExprList exprs,
                                            ExecutionContext execCxt)
    {
        // Execute a BGP on the real default graph
        return optimizeExecuteTriples(graph, input, opBGP.getPattern(), exprs, execCxt) ;
    }

    private static QueryIterator optimizeExecuteTriples(LinkedDataFragmentGraph graph,
                                                        QueryIterator input, BasicPattern pattern, ExprList exprs,
                                                        ExecutionContext execCxt) {
        if ( ! input.hasNext() ) {
            return input ;
        }

        // -- Input
        // Must pass this iterator into the next stage.
        if ( pattern.size() >= 2 ) {
            // Must be 2 or triples to reorder.
            ReorderTransformation transform = graph.getReorderTransform() ;
            if ( transform != null )
            {
                QueryIterPeek peek = QueryIterPeek.create(input, execCxt) ;
                input = peek ; // Must pass on
                pattern = reorder(pattern, peek, transform) ;
            }
        }
        // -- Filter placement

        Op op = null ;
        if ( exprs != null )
            op = TransformFilterPlacement.transform(exprs, pattern) ;
        else
            op = new OpBGP(pattern) ;

        return plainExecute(op, input, execCxt) ;
    }

    /** Execute without modification of the op - does <b>not</b> apply special graph name translations */
    private static QueryIterator plainExecute(Op op, QueryIterator input, ExecutionContext execCxt) {
        // -- Execute
        // Switch to a non-reordering executor
        // The Op may be a sequence due to TransformFilterPlacement
        // so we need to do a full execution step, not go straight to the SolverLib.

        ExecutionContext ec2 = new ExecutionContext(execCxt) ;
        ec2.setExecutor(plainFactory) ;

        // Solve without going through this executor again.
        // There would be issues of nested patterns but this is only a
        // (filter (bgp...)) or (filter (quadpattern ...)) or sequences of these.
        // so there are no nested patterns to reorder.
        return QC.execute(op, input, ec2) ;
    }

    private static BasicPattern reorder(BasicPattern pattern, QueryIterPeek peek, ReorderTransformation transform) {
        if ( transform != null ) {
            // This works by getting one result from the peek iterator,
            // and creating the more gounded BGP. The tranform is used to
            // determine the best order and the transformation is returned. This
            // transform is applied to the unsubstituted pattern (which will be
            // substituted as part of evaluation.

            if ( ! peek.hasNext() ) {
                throw new ARQInternalErrorException("Peek iterator is already empty") ;
            }
            
            BasicPattern pattern2 = Substitute.substitute(pattern, peek.peek() ) ;
            // Calculate the reordering based on the substituted pattern.
            ReorderProc proc = transform.reorderIndexes(pattern2) ;
            // Then reorder original patten
            pattern = proc.reorder(pattern) ;
        }
        return pattern ;
    }

    private static OpExecutorFactory plainFactory = new OpExecutorPlainFactoryLDF() ;
    private static class OpExecutorPlainFactoryLDF implements OpExecutorFactory {
        @Override
        public OpExecutor create(ExecutionContext execCxt) {
            return new OpExecutorPlainLDF(execCxt) ;
        }
    }

    /** An op executor that simply executes a BGP or QuadPattern without any reordering */
    private static class OpExecutorPlainLDF extends OpExecutor {
        public OpExecutorPlainLDF(ExecutionContext execCxt) {
            super(execCxt) ;
        }

        @Override
        public QueryIterator execute(OpBGP opBGP, QueryIterator input) {
            return super.execute(opBGP, input) ;
        }
    }

}
