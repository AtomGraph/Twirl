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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class InfOntModelTemplateImplTest
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
    
    @Test
    public void testArguments()
    {
        Resource argument = getOntModel().createIndividual("http://ontology/arg", SPL.Argument).
                addProperty(SPL.predicate, FOAF.name);
        Resource template = getOntModel().createIndividual("http://ontology/template", SPIN.Template).
                addProperty(SPIN.body, getOntModel().createIndividual(SP.Construct).
                        addLiteral(SP.text, "CONSTRUCT WHERE { ?s ?p ?o }")).
                addProperty(SPIN.constraint, argument);
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", template);
        
        assertEquals(argument.as(Argument.class), constraint.as(TemplateCall.class).getTemplate().getArguments(false).get(0));
    }
    
    public OntModel getOntModel()
    {
        return ontModel;
    }
    
}
