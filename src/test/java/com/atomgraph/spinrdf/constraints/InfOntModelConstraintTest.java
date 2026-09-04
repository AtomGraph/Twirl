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
import java.util.List;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    public void namedInstanceViolationRoot()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource instance = getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(1, cvs.size());
        assertEquals(instance, cvs.get(0).getRoot()); // spin:violationRoot ?this must resolve to the checked instance
    }

    // the checked instance is a blank node (e.g. an unsaved resource in a POSTed request body), and the
    // violation root must still be that very node — not a bnode freshly minted by CONSTRUCT template
    // instantiation. ?this only round-trips through the query for IRIs; for bnodes the identity has to
    // survive by other means, which is what these tests pin down
    @Test
    public void anonInstanceViolationRoot()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource instance = getOntModel().createResource().addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(1, cvs.size());
        assertEquals(instance, cvs.get(0).getRoot());
    }

    @Test
    public void anonInstanceViolationRootRDF()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        Resource instance = getOntModel().createResource().addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(1, cvs.size());

        // add the violations into the model that holds the instance, the way a validating server merges
        // them into the request model for the error response
        SPINConstraints.addConstraintViolationsRDF(cvs, getOntModel(), true);
        assertTrue(getOntModel().contains(null, SPIN.violationRoot, instance)); // root must not dangle
    }

    // the violation message is the constraint's own authored rdfs:label - the channel the SPINConstraints
    // rewrite severed when it stopped consulting the constraint resource. No authored label means no
    // message at all: boilerplate fallbacks ("SPIN constraint at ...") must not masquerade as authored text
    @Test
    public void violationMessageFromConstraintLabel()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger)).
                addProperty(RDFS.label, "Missing foaf:name");
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(1, cvs.size());
        assertEquals("Missing foaf:name", cvs.get(0).getMessage());
    }

    @Test
    public void violationMessageAbsentWithoutLabel()
    {
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(1, cvs.size());
        assertNull(cvs.get(0).getMessage());

        Model result = ModelFactory.createDefaultModel();
        SPINConstraints.addConstraintViolationsRDF(cvs, result, true);
        assertFalse(result.contains(null, RDFS.label, (RDFNode) null));
    }

    @Test
    public void violationMessagesIndependentPerViolation()
    {
        Resource template = getOntModel().createResource("http://ontology/template").addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, getOntModel().createResource().addProperty(RDF.type, SP.Construct).
                        addProperty(SP.text, """
                            PREFIX spin: <http://spinrdf.org/spin#>
                            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
                            CONSTRUCT {
                                _:a a spin:ConstraintViolation .
                                _:a spin:violationRoot ?this .
                                _:a rdfs:label "labelled violation" .
                                _:b a spin:ConstraintViolation .
                                _:b spin:violationRoot ?this .
                            }
                            WHERE {}"""));
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, template);
        Resource cls = getOntModel().createResource("http://ontology/class").addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);

        getOntModel().createResource("http://data/instance").addProperty(RDF.type, cls);

        List<ConstraintViolation> cvs = SPINConstraints.check(getOntModel());
        assertEquals(2, cvs.size());
        // one violation carries its CONSTRUCT-emitted label, the other has none - it must not inherit
        // the first one's label (the loop used its label variable as an accumulator) nor grow a fallback
        assertEquals(1, cvs.stream().filter(cv -> "labelled violation".equals(cv.getMessage())).count());
        assertEquals(1, cvs.stream().filter(cv -> cv.getMessage() == null).count());
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
