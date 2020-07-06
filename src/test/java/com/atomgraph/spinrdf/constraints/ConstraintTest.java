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
import java.util.Collections;
import java.util.Map;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.AnonId;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ConstraintTest
{
    private static final int SYSTEM_CONSTRAINT_COUNT = 9;
    
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
    
    @Test
    public void validateSystem()
    {
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }
    
    public void countSystemConstraints()
    {
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(ontModel, SPIN.constraint).size());
    }
    
    @Test
    public void missingTemplateBody()
    {
        Resource template = ontModel.createIndividual("http://ontology/template", SPIN.Template);
        Resource constraint = ontModel.createIndividual("http://ontology/constraint", template);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(ontModel, SPIN.constraint).size()); // constraint ignored
    }

    @Test
    public void missingQueryText()
    {
        Resource template = ontModel.createIndividual("http://ontology/template", SPIN.Template);
        Resource constraint = ontModel.createIndividual("http://ontology/template", template).
                addProperty(SPIN.body, ontModel.createIndividual(SP.Construct));
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(ontModel, SPIN.constraint).size()); // constraint ignored
    }

    @Test(expected = QueryParseException.class)
    public void queryTextSyntaxError()
    {
        Resource template = ontModel.createIndividual("http://ontology/template", SPIN.Template).
                addProperty(SPIN.body, ontModel.createIndividual(SP.Construct).
                        addProperty(SP.text, "not SPARQL"));
        Resource constraint = ontModel.createIndividual("http://ontology/template", template);
        ontModel.createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(ontModel, SPIN.constraint).size());
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
    
//    @Test
//    public void bnodeQueryTest()
//    {
//        Model model = ModelFactory.createDefaultModel();
//        Resource bnode = model.createResource().addProperty(FOAF.name, "whateverest");
//        AnonId id = bnode.getId();
//        
//        Query query = QueryFactory.create("SELECT * { ?s ?p ?o }");
////        QuerySolutionMap qsm = new QuerySolutionMap();
////        qsm.add("s", model.createResource("_:" + id));
//        
//        Map<String, RDFNode> substitutions = Collections.singletonMap("s", bnode);
//        query = QueryTransformOps.transformQuery(query, substitutions);
//
//        try (QueryExecution qex = QueryExecutionFactory.create(query, model))
//        {
//            ResultSet resultSet = qex.execSelect();
//            ResultSetFormatter.out(System.out, resultSet);
//            assertTrue(resultSet.hasNext());
//        }
//    }
    
}
