/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.constraints;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public abstract class SimplePropertyPath
{

    private final Property predicate;
    private final Resource root;
    
    public SimplePropertyPath(Resource root, Property predicate)
    {
        this.predicate = predicate;
        this.root = root;
    }
    
    public Property getPredicate()
    {
        return predicate;
    }
    
    public Resource getRoot()
    {
        return root;
    }
    
    @Override
    public String toString()
    {
        return root + " " + predicate;
    }
        
}
