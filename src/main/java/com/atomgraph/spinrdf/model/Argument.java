/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public interface Argument extends Resource
{
    
    Property getPredicate();
    
    Resource getValueType();
    
    boolean isOptional();
    
    RDFNode getDefaultValue();
    
    String getVarName();
    
    Integer getArgIndex();

    String getComment();
    
}
