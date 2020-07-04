/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model;

import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class CommandImplTest
{
    
    private OntModel ontModel;
    private Ontology ontology;

    static
    {
        JenaSystem.init();
    }
    
    @BeforeClass
    public static void init()
    {
        com.atomgraph.spinrdf.vocabulary.SP.init(BuiltinPersonalities.model);
    }
    
    public OntModel getOntModel()
    {
        return ModelFactory.createOntologyModel();
    }
    
    @Before
    public void ontology()
    {
        ontModel = getOntModel();
        
        ontology = ontModel.createOntology("http://ontology/");
        ontology.addImport(ResourceFactory.createResource(SP.NS));
        ontology.addImport(ResourceFactory.createResource(SPIN.NS));
        ontology.addImport(ResourceFactory.createResource(SPL.NS));
        ontology.addImport(FOAF.NAMESPACE);
        ontModel.loadImports();
    }

    @Test(expected = PropertyNotFoundException.class)
    public void missingTemplateBody()
    {
        ontModel.createIndividual(SPIN.Template).as(Template.class).getBody(); // missing spin:body
    }

    @Test(expected = PropertyNotFoundException.class)
    public void missingQueryText()
    {
        ontModel.createIndividual(SP.Construct).as(Query.class).getText(); // missing sp:text
    }

    
}
