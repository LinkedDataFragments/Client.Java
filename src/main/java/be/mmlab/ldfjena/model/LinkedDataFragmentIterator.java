package be.mmlab.ldfjena.model;

import be.mmlab.ldfjena.LinkedDataFragmentsClient;
import com.hp.hpl.jena.graph.TripleMatch;

import java.util.Iterator;

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
