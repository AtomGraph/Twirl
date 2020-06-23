/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class SPL
{
    
    public final static String BASE_URI = "http://spinrdf.org/spl";
    
    public final static String NS = BASE_URI + "#";
    
    public final static String PREFIX = "spl";
    

    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
    public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
    public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");
   
    public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
    
    public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
    
    public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
    
    public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
        
}
