package org.linkeddatafragments.solver;

/**
 * Created by ldevocht on 4/28/14.
 */
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.ARQInternalErrorException;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryEngineFactory;
import com.hp.hpl.jena.sparql.engine.QueryEngineRegistry;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.main.QueryEngineMain;
import com.hp.hpl.jena.sparql.util.Context;

public class LinkedDataFragmentEngine extends QueryEngineMain {

    protected Query ldfQuery;
    protected DatasetGraph ldfDataset;
    protected Binding ldfBinding;
    protected Context ldfContext;

    public LinkedDataFragmentEngine(Query query, DatasetGraph dataset, Binding input, Context context)
    {
        super(query, dataset, input, context) ;
        this.ldfQuery = query;
        this.ldfDataset = dataset;
        this.ldfBinding = input;
        this.ldfContext = context;
    }

    public LinkedDataFragmentEngine(Op op, DatasetGraph dataset, Binding input, Context context) {
        super(op, dataset, input, context);
    }

    // ---- Registration of the factory for this query engine class.

    // Query engine factory.
    // Call LinkedDataFragmentEngine.register() to add to the global query engine registry.

    static QueryEngineFactory factory = new LinkedDataFragmentEngineFactory() ;

    static public QueryEngineFactory getFactory() {
        return factory;
    }

    static public void register(){
        QueryEngineRegistry.addFactory(factory) ;
    }

    static public void unregister(){
        QueryEngineRegistry.removeFactory(factory);
    }

    static class LinkedDataFragmentEngineFactory implements QueryEngineFactory {

        public boolean accept(Query query, DatasetGraph dataset, Context context) {
            return true;
        }

        public Plan create(Query query, DatasetGraph dataset, Binding initial, Context context) {
            LinkedDataFragmentEngine engine = new LinkedDataFragmentEngine(query, dataset, initial, context);
            return engine.getPlan();
        }

        public boolean accept(Op op, DatasetGraph dataset, Context context) {
            // Refuse to accept algebra expressions directly.
            return false;
        }

        public Plan create(Op op, DatasetGraph dataset, Binding inputBinding, Context context) {
            // Should not be called because accept/Op is false
            throw new ARQInternalErrorException("LDFQueryEngine: factory called directly with an algebra expression");
        }
    }
}