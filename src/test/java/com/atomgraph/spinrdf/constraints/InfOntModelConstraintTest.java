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

import com.atomgraph.spinrdf.SpinSpecifications;
import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises {@link SPINConstraints} against an RDFS-inferred ontapi {@link OntModel} whose personality carries the
 * SPIN polymorphism (see {@link SpinSpecifications}). The SPIN system ontologies (sp/spin/spl/foaf) are read in
 * directly rather than resolved through the legacy {@code OntDocumentManager} import mechanism.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InfOntModelConstraintTest
{
    private static final int SYSTEM_CONSTRAINT_COUNT = 9;

    private OntModel ontModel;

    static
    {
        JenaSystem.init();
    }

    public OntModel createOntModel()
    {
        return OntModelFactory.createModel(SpinSpecifications.spinAware(OntSpecification.OWL1_FULL_MEM_RDFS_INF));
    }

    @BeforeEach
    public void ontology()
    {
        ontModel = createOntModel();

        // SPIN system ontologies (bundled under etc/) — read in directly instead of owl:imports + loadImports()
        RDFDataMgr.read(ontModel, "etc/sp.ttl");
        RDFDataMgr.read(ontModel, "etc/spin.ttl");
        RDFDataMgr.read(ontModel, "etc/spl.spin.ttl");
        RDFDataMgr.read(ontModel, "etc/foaf.owl");
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
        Resource template = getOntModel().createResource("http://ontology/template").addProperty(RDF.type, SPIN.Template);
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, template);
        getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size()); // constraint ignored
    }

    @Test
    public void missingQueryText()
    {
        Resource template = getOntModel().createResource("http://ontology/template").addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, getOntModel().createResource().addProperty(RDF.type, SP.Construct));
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, template);
        getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        assertEquals(SYSTEM_CONSTRAINT_COUNT, SPINConstraints.class2Query(getOntModel(), SPIN.constraint).size()); // constraint ignored
    }

    @Test
    public void queryTextSyntaxError()
    {
        Resource template = getOntModel().createResource("http://ontology/template").addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, getOntModel().createResource().addProperty(RDF.type, SP.Construct).
                        addProperty(SP.text, "not SPARQL"));
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, template);
        getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        assertThrows(QueryParseException.class, () ->
                SPINConstraints.class2Query(getOntModel(), SPIN.constraint));
    }

    @Test
    public void classInheritance1()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createResource("http://ontology/super-class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/super-instance").addProperty(RDF.type, superCls);
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void classInheritance2()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createResource("http://ontology/super-class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(RDFS.subClassOf, superCls);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);
        assertEquals(2, SPINConstraints.check(getOntModel()).size()); // because the instance is also inferred to be an instance of the super-class
    }

    @Test
    public void classInheritance3()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource superCls = getOntModel().createResource("http://ontology/super-class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(RDFS.subClassOf, superCls);
        Resource subCls = getOntModel().createResource("http://ontology/sub-class").addProperty(RDF.type, RDFS.Class).
                addProperty(RDFS.subClassOf, cls);

        getOntModel().createResource("http://data/sub-instance").addProperty(RDF.type, subCls);
        assertEquals(3, SPINConstraints.check(getOntModel()).size()); // because the instance is also inferred to be an instance of the super-classes
    }

    @Test
    public void invalidMinCount()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void invalidMaxCount()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.maxCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addLiteral(FOAF.name, "one").
                addLiteral(FOAF.name, "two");

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void invalidResourceValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource notPerson = getOntModel().createResource("http://ontology/not-person").addProperty(RDF.type, FOAF.Group);
        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addProperty(FOAF.maker, notPerson);

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void invalidResourceSubClassValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource subClass = getOntModel().createResource("http://data/not-person-subclass").
                addProperty(RDFS.subClassOf, FOAF.Group);
        Resource notPerson = getOntModel().createResource("http://ontology/not-person").addProperty(RDF.type, subClass);
        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addProperty(FOAF.maker, notPerson);

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void validResourceValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource person = getOntModel().createResource("http://data/person").addProperty(RDF.type, FOAF.Person);
        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addProperty(FOAF.maker, person);

        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void validResourceSubClassValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.maker).
                addProperty(SPL.valueType, FOAF.Person);
        Resource cls = getOntModel().createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource subClass = getOntModel().createResource("http://data/concept-subclass").
                addProperty(RDFS.subClassOf, FOAF.Person);
        Resource person = getOntModel().createResource("http://data/concept").addProperty(RDF.type, subClass);
        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addProperty(FOAF.maker, person);

        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void validStringValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addLiteral(FOAF.name, "literal");

        assertEquals(0, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    public void invalidStringValueType()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls).
                addLiteral(FOAF.name, 42);

        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    public OntModel getOntModel()
    {
        return ontModel;
    }

}
