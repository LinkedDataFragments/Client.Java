package org.linkeddatafragments.solver;

import java.util.Iterator;

import org.apache.jena.atlas.iterator.Iter;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * Copied from Jena distribution because the constructor was protected
 *
 *
 */

public class BindingOne implements Binding
{
    private final Var var ;
    private final Node value ;

    public BindingOne(Var var, Node node)
    {
        this.var = var;
        this.value = node;
    }

    public int size() { return 1 ; }

    public boolean isEmpty() { return false ; }

    /** Iterate over all the names of variables.
     */
    public Iterator<Var> vars()
    {
        return Iter.singleton(var) ;
    }

    public boolean contains(Var n)
    {
        return var.equals(n) ;
    }

    public Node get(Var v)
    {
        if ( v.equals(var) )
            return value ;
        return null ;
    }
}
