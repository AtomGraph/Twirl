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
package com.atomgraph.spinrdf.constraints;

import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ConstraintTest
{
    private OntModel ontModel;
    private Ontology ontology;

    @BeforeClass
    public static void init()
    {
        JenaSystem.init();
        com.atomgraph.spinrdf.vocabulary.SP.init(BuiltinPersonalities.model);
    }
    
    @Before
    public void ontology()
    {
        ontModel = ModelFactory.createOntologyModel();
        
        ontology = ontModel.createOntology("http://ontology/");
        ontology.addImport(ResourceFactory.createResource(SP.NS));
        ontology.addImport(ResourceFactory.createResource(SPIN.NS));
        ontology.addImport(ResourceFactory.createResource(SPL.NS));
        ontology.addImport(FOAF.NAMESPACE);
        ontModel.loadImports();
    }
    
    @Test
    public void validateSystem()
    {
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void invalidMinCount()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class"));
        
        assertEquals(1, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void invalidMaxCount()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.maxCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "one").
                addLiteral(FOAF.name, "two");
        
        assertEquals(1, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void invalidResourceValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource notPerson = ontModel.createIndividual("http://ontology/not-person", FOAF.Group);
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addProperty(FOAF.maker, notPerson);
        
        assertEquals(1, SPINConstraints.check(ontModel).size());
    }

    @Test
    public void invalidResourceSubClassValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource subClass = ontModel.createResource("http://data/not-person-subclass").
                addProperty(RDFS.subClassOf, FOAF.Group);
        Resource notPerson = ontModel.createIndividual("http://ontology/not-person", subClass);
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addProperty(FOAF.maker, notPerson);
        
        assertEquals(1, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void validResourceValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource person = ontModel.createIndividual("http://data/person", FOAF.Person);
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addProperty(FOAF.maker, person);
        
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }

    @Test
    public void validResourceSubClassValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource subClass = ontModel.createResource("http://data/concept-subclass").
                addProperty(RDFS.subClassOf, FOAF.Person);
        Resource person = ontModel.createIndividual("http://data/concept", subClass);
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addProperty(FOAF.maker, person);
        
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void validStringValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "literal");
        
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }
    
    @Test
    public void invalidStringValueType()
    {
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        ontModel.createIndividual("http://data/instance", ontModel.createResource("http://ontology/class")).
                addLiteral(FOAF.name, 42);

        assertEquals(1, SPINConstraints.check(ontModel).size());
    }
    
}
