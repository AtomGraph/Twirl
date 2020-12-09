/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
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
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InfOntModelCommandImplTest
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
    
    public OntModel createOntModel()
    {
        return ModelFactory.createOntologyModel();
    }
    
    @Before
    public void ontology()
    {
        ontModel = createOntModel();
        
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
        getOntModel().createIndividual(SPIN.Template).as(Template.class).getBody(); // missing spin:body
    }

    @Test(expected = PropertyNotFoundException.class)
    public void missingQueryText()
    {
        getOntModel().createIndividual(SP.Construct).as(Query.class).getText(); // missing sp:text
    }

    public OntModel getOntModel()
    {
        return ontModel;
    }
    
}
