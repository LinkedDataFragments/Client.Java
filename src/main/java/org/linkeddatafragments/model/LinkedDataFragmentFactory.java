package org.linkeddatafragments.model;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Created with IntelliJ IDEA.
 * User: ldevocht
 * Date: 2/13/14
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class LinkedDataFragmentFactory {
    public static LinkedDataFragment create(final ExtendedIterator<Triple> triples, final Long matchCount, final Triple match) {
        return new LinkedDataFragment(triples, matchCount, match);
    }

    public static LinkedDataFragment create(final ExtendedIterator<Triple> triples, final Triple match) {
        return new LinkedDataFragment(triples, match);
    }

    public static LinkedDataFragment create(final Triple match) {
        return new LinkedDataFragment(match);
    }

    public static LinkedDataFragment create(final Triple match, final Long matchCount) {
        return new LinkedDataFragment(match, matchCount);
    }
}
