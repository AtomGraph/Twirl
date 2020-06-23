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

import com.atomgraph.spinrdf.constraints.SPINConstraints.QueryWrapper;
import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
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
        
        Ontology ontology = ontModel.createOntology("http://ontology/");
        ontology.addImport(ResourceFactory.createResource(SP.NS));
        ontology.addImport(ResourceFactory.createResource(SPIN.NS));
        ontology.addImport(ResourceFactory.createResource(SPL.NS));
        ontModel.loadImports();
    }

    public List<ConstraintViolation> validate(Model model)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        Map<Resource, List<QueryWrapper>> class2Query = SPINConstraints.class2Query(ontModel);
        for (Resource cls : class2Query.keySet())
        {
            List<QueryWrapper> wrappers = class2Query.get(cls);
            for (QueryWrapper wrapper : wrappers)
                SPINConstraints.runQueryOnClass(cvs, wrapper, cls, model);
        }
        
        return cvs;
    }
    
    @Test
    public void validateSystem()
    {
        assertEquals(0, validate(ontModel).size());
    }
    
    @Test
    public void invalidMinCount()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, 1);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class"));
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void invalidMaxCount()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.maxCount, 1);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "one").
                addLiteral(FOAF.name, "two");
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void invalidResourceValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource notConcept = model.createResource("http://ontology/not-skos-concept").
                addProperty(RDF.type, SKOS.Collection);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, notConcept);
        
        assertEquals(1, validate(model).size());
    }

    @Test
    public void invalidResourceSubClassValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource subClass = model.createResource("http://data/not-concept-subclass").
                addProperty(RDFS.subClassOf, SKOS.Collection);
        Resource notConcept = model.createResource("http://ontology/not-skos-concept").
                addProperty(RDF.type, subClass);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, notConcept);
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void validResourceValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource concept = model.createResource("http://data/concept").
                addProperty(RDF.type, SKOS.Concept);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, concept);
        
        assertEquals(0, validate(model).size());
    }

    @Test
    public void validResourceSubClassValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource subClass = model.createResource("http://data/concept-subclass").
                addProperty(RDFS.subClassOf, SKOS.Concept);
        Resource concept = model.createResource("http://data/concept").
                addProperty(RDF.type, subClass);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, concept);
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void validLiteralValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, RDFS.Literal);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint).
                addProperty(FOAF.name, SKOS.Collection); // will not cause violation since isLiteral(?object) = false
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(FOAF.name, "literal");
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void validStringValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "literal");
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void invalidStringValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, 42);

        assertEquals(1, validate(model).size());
    }
    
}
