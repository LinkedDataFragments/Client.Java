package org.linkeddatafragments.model;

import org.linkeddatafragments.model.LinkedDataFragment;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Created with IntelliJ IDEA.
 * User: ldevocht
 * Date: 2/13/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedDataFragmentFactory {
    public static LinkedDataFragment create(ExtendedIterator<Triple> triples, Long matchCount, TripleMatch m) {
        return new LinkedDataFragment(triples, matchCount, m);
    }

    public static LinkedDataFragment create(ExtendedIterator<Triple> triples, TripleMatch m) {
        return new LinkedDataFragment(triples, m);
    }

    public static LinkedDataFragment create(TripleMatch m) {
        return new LinkedDataFragment(m);
    }

    public static LinkedDataFragment create(TripleMatch m, Long matchCount) {
        return new LinkedDataFragment(m, matchCount);
    }
}
