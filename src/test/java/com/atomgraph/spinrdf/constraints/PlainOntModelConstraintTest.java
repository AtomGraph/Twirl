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
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 * Same constraints as {@link InfOntModelConstraintTest} but over a non-inferring ontapi {@link OntModel}, so a
 * constrained super-class only fires on instances reached via {@code SPINConstraints}' own subclass walk — not via
 * RDFS type propagation. Hence the {@code classInheritance*} counts drop.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class PlainOntModelConstraintTest extends InfOntModelConstraintTest
{

    static
    {
        JenaSystem.init();
    }

    @Override
    public OntModel createOntModel()
    {
        return OntModelFactory.createModel(SpinSpecifications.spinAware(OntSpecification.OWL1_FULL_MEM));
    }

    @Test
    @Override
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
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    @Override
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
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

}
