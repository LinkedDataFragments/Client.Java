package org.linkeddatafragments.model;

import com.hp.hpl.jena.graph.TripleMatch;

import java.util.Iterator;

import org.linkeddatafragments.client.LinkedDataFragmentsClient;

/**
 * Created by ldevocht on 4/25/14.
 */
public class LinkedDataFragmentIterator implements Iterator<LinkedDataFragment> {
    protected final LinkedDataFragment baseFragment;
    protected final TripleMatch tripleTemplate;
    protected final LinkedDataFragmentsClient ldfClient;

    protected LinkedDataFragment currentFragment;

    public LinkedDataFragmentIterator(LinkedDataFragment ldf, LinkedDataFragmentsClient c) {
        baseFragment = ldf;
        tripleTemplate = ldf.tripleMatch;
        currentFragment = baseFragment;
        ldfClient = c;
    }

    public static LinkedDataFragmentIterator create(LinkedDataFragment ldf, LinkedDataFragmentsClient c) {
        return new LinkedDataFragmentIterator(ldf, c);
    }

    public boolean hasNext() {
        return currentFragment.hasNextUrl();
    }

    public LinkedDataFragment next() {
        try {
            currentFragment = ldfClient.getFragment("GET", currentFragment.getNextUrl(), this.tripleTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return currentFragment;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
