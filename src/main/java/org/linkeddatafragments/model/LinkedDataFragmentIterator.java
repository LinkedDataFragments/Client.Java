package org.linkeddatafragments.model;

import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.linkeddatafragments.client.LinkedDataFragmentsClient;

/**
 * Created by ldevocht on 4/25/14.
 */
public class LinkedDataFragmentIterator implements Iterator<LinkedDataFragment> {
    protected final LinkedDataFragment baseFragment;
    protected final Triple tripleTemplate;
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

    @Override
    public boolean hasNext() {
        return currentFragment.hasNextUrl();
    }

    @Override
    public LinkedDataFragment next() {
        try {
            currentFragment = ldfClient.getFragment("GET", currentFragment.getNextUrl(), this.tripleTemplate);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return currentFragment;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
