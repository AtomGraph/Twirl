/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.util;

import java.util.Arrays;
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class JenaUtilTest
{
    
    private Model model;
    
    @Before
    public void model()
    {
        model = ModelFactory.createDefaultModel();

        Resource superSuperClass = model.createResource("http://ontology/super-super-class").
                addProperty(RDF.type, RDFS.Class);

        Resource superClass = model.createResource("http://ontology/super-class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(RDFS.subClassOf, superSuperClass);
        
        model.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(RDFS.subClassOf, superClass);
    }
    
    @Test
    public void testGet2SuperClasses()
    {
        Resource cls = model.createResource("http://ontology/class");
        List<Resource> expected = Arrays.asList(cls,
                model.createResource("http://ontology/super-class"),
                model.createResource("http://ontology/super-super-class"));
        
        assertEquals(expected, com.atomgraph.spinrdf.util.JenaUtil.getSuperClasses(cls));
    }
    
}
