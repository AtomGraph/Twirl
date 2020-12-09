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
import org.apache.jena.query.QueryParseException;
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
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InfOntModelConstraintTest
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
    public void validateSystem()
    {
        assertEquals(0, SPINConstraints.check(ontModel).size());
    }
    
    public void countSystemConstraints()
    {
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size());
    }
    
    @Test
    public void missingTemplateBody()
    {
        Resource template = getOntModel().createIndividual("http://ontology/template", SPIN.Template);
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", template);
        getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size()); // constraint ignored
    }

    @Test
    public void missingQueryText()
    {
        Resource template = getOntModel().createIndividual("http://ontology/template", SPIN.Template).
                addProperty(SPIN.body, getOntModel().createIndividual(SP.Construct));
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", template);
        getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size()); // constraint ignored
    }

    @Test(expected = QueryParseException.class)
    public void queryTextSyntaxError()
    {
        Resource template = getOntModel().createIndividual("http://ontology/template", SPIN.Template).
                addProperty(SPIN.body, getOntModel().createIndividual(SP.Construct).
                        addProperty(SP.text, "not SPARQL"));
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", template);
        getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size());
    }

    @Test
    public void classInheritance1()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createIndividual("http://ontology/super-class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        getOntModel().createIndividual("http://data/super-instance", superCls);
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void classInheritance2()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createIndividual("http://ontology/super-class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(RDFS.subClassOf, superCls);
        
        getOntModel().createIndividual("http://data/instance", cls);
        assertEquals(2, SPINConstraints.check(getOntModel()).size()); // because the instance is also inferred to be an instance of the super-class
    }

    @Test
    public void classInheritance3()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createIndividual("http://ontology/super-class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(RDFS.subClassOf, superCls);
        Resource subCls = getOntModel().createIndividual("http://ontology/sub-class", RDFS.Class).
                addProperty(RDFS.subClassOf, cls);
        
        getOntModel().createIndividual("http://data/sub-instance", subCls);
        assertEquals(3, SPINConstraints.check(getOntModel()).size()); // because the instance is also inferred to be an instance of the super-classes
    }

    @Test
    public void invalidMinCount()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        getOntModel().createIndividual("http://data/instance", cls);
        
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }
    
    @Test
    public void invalidMaxCount()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.maxCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        getOntModel().createIndividual("http://data/instance", cls).
                addLiteral(FOAF.name, "one").
                addLiteral(FOAF.name, "two");
        
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }
    
    @Test
    public void invalidResourceValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource notPerson = getOntModel().createIndividual("http://ontology/not-person", FOAF.Group);
        getOntModel().createIndividual("http://data/instance", cls).
                addProperty(FOAF.maker, notPerson);
        
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void invalidResourceSubClassValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource subClass = getOntModel().createResource("http://data/not-person-subclass").
                addProperty(RDFS.subClassOf, FOAF.Group);
        Resource notPerson = getOntModel().createIndividual("http://ontology/not-person", subClass);
        getOntModel().createIndividual("http://data/instance", cls).
                addProperty(FOAF.maker, notPerson);
        
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }
    
    @Test
    public void validResourceValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource person = getOntModel().createIndividual("http://data/person", FOAF.Person);
        getOntModel().createIndividual("http://data/instance", cls).
                addProperty(FOAF.maker, person);
        
        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void validResourceSubClassValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Resource subClass = getOntModel().createResource("http://data/concept-subclass").
                addProperty(RDFS.subClassOf, FOAF.Person);
        Resource person = getOntModel().createIndividual("http://data/concept", subClass);
        getOntModel().createIndividual("http://data/instance", cls).
                addProperty(FOAF.maker, person);
        
        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }
    
    @Test
    public void validStringValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        getOntModel().createIndividual("http://data/instance", cls).
                addLiteral(FOAF.name, "literal");
        
        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }
    
    @Test
    public void invalidStringValueType()
    {
        Resource constraint = getOntModel().createIndividual("http://ontology/constraint", SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        Resource cls = getOntModel().createIndividual("http://ontology/class", RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        getOntModel().createIndividual("http://data/instance", cls).
                addLiteral(FOAF.name, 42);

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }
    
    public OntModel getOntModel()
    {
        return ontModel;
    }
    
}
