package be.mmlab.ldfjena.model;

import com.hp.hpl.jena.graph.Capabilities;

public class LinkedDataFragmentGraphCapabilities implements Capabilities
{
    @Override
    public boolean sizeAccurate() { return true; }
    @Override
    public boolean addAllowed() { return addAllowed( false ); }
    @Override
    public boolean addAllowed( boolean every ) { return every; }
    @Override
    public boolean deleteAllowed() { return deleteAllowed( false ); }
    @Override
    public boolean deleteAllowed( boolean every ) { return every; }
    @Override
    public boolean canBeEmpty() { return false; }
    @Override
    public boolean iteratorRemoveAllowed() { return false; }
    @Override
    public boolean findContractSafe() { return true; }
    @Override
    public boolean handlesLiteralTyping() { return true; }
}
