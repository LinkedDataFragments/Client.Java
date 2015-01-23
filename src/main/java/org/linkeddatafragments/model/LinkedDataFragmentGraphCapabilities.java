package org.linkeddatafragments.model;

import com.hp.hpl.jena.graph.Capabilities;

public class LinkedDataFragmentGraphCapabilities implements Capabilities
{
    public boolean sizeAccurate() { return true; }
    public boolean addAllowed() { return addAllowed( false ); }
    public boolean addAllowed( boolean every ) { return every; }
    public boolean deleteAllowed() { return deleteAllowed( false ); }
    public boolean deleteAllowed( boolean every ) { return every; }
    public boolean canBeEmpty() { return false; }
    public boolean iteratorRemoveAllowed() { return false; }
    public boolean findContractSafe() { return true; }
    public boolean handlesLiteralTyping() { return true; }
}
