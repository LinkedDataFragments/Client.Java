package org.linkeddatafragments.model;


import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.jena.graph.Triple;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.Filter;
import org.apache.jena.util.iterator.Map1;
import org.linkeddatafragments.client.LinkedDataFragmentsClient;

/**
 * Created by ldevocht on 4/29/14.
 */
public class ExtendedTripleIteratorLDF implements ExtendedIterator<Triple> {
    protected ExtendedIterator<Triple> triples;
    protected Iterator<LinkedDataFragment> ldfIterator;

    public ExtendedTripleIteratorLDF(LinkedDataFragmentsClient ldfClient, LinkedDataFragment ldf) {
        triples = ldf.getTriples();
        ldfIterator = LinkedDataFragmentIterator.create(ldf, ldfClient);
    }

    public static ExtendedIterator<Triple> create(LinkedDataFragmentsClient ldfClient, LinkedDataFragment ldf) {
        return new ExtendedTripleIteratorLDF(ldfClient, ldf);
    }

    @Override
    public Triple removeNext() {
        return triples.removeNext();
    }

    @Override
    public <X extends Triple> ExtendedIterator<Triple> andThen(Iterator<X> other) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExtendedIterator<Triple> filterKeep(Predicate<Triple> predicate) {
        return triples.filterKeep(predicate);
    }

    @Override
    public ExtendedIterator<Triple> filterDrop(Predicate<Triple> predicate) {
        return triples.filterDrop(predicate);
    }

    @Override
    public <U> ExtendedIterator<U> mapWith(Function<Triple, U> function) {
        return triples.mapWith(function);
    }

    @Override
    public List<Triple> toList() {
        return triples.toList();
    }

    @Override
    public Set<Triple> toSet() {
        return triples.toSet();
    }

    @Override
    public void close() {
        triples.close();
    }

    @Override
    public boolean hasNext() {
        waitForFragmentTriplesReady();
        Boolean hasNext = triples.hasNext();
        if(!hasNext) {
            if(ldfIterator.hasNext()) {
                triples = ldfIterator.next().getTriples();
                if(triples.hasNext()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return hasNext;
        }
    }

    @Override
    public Triple next() {
        waitForFragmentTriplesReady();
        Boolean hasNext = triples.hasNext();
        if(!hasNext) {
            if(ldfIterator.hasNext()) {
                triples = ldfIterator.next().getTriples();
                if(triples.hasNext()) {
                    return triples.next();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return triples.next();
        }
    }

    @Override
    public void remove() {

    }
}
