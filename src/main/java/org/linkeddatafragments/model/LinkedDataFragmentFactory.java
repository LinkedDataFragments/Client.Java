package org.linkeddatafragments.model;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;


/**
 * Created with IntelliJ IDEA.
 * User: ldevocht
 * Date: 2/13/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedDataFragmentFactory {
    public static LinkedDataFragment create(ExtendedIterator<Triple> triples, Long matchCount, Triple m) {
        return new LinkedDataFragment(triples, matchCount, m);
    }

    public static LinkedDataFragment create(ExtendedIterator<Triple> triples, Triple m) {
        return new LinkedDataFragment(triples, m);
    }

    public static LinkedDataFragment create(Triple m) {
        return new LinkedDataFragment(m);
    }

    public static LinkedDataFragment create(Triple m, Long matchCount) {
        return new LinkedDataFragment(m, matchCount);
    }
}
