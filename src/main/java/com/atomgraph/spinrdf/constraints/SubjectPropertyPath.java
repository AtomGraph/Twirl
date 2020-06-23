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
public class SubjectPropertyPath extends SimplePropertyPath
{
    
    public SubjectPropertyPath(Resource root, Property predicate)
    {
        super(root, predicate);
    }
    
}
