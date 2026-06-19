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

import com.atomgraph.spinrdf.SpinSpecifications;
import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises SPIN template/template-call/argument polymorphism directly on an RDFS-inferred ontapi {@link OntModel}
 * whose personality carries the SPIN implementations (see {@link SpinSpecifications}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InfOntModelTemplateImplTest
{

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

        RDFDataMgr.read(ontModel, "etc/sp.ttl");
        RDFDataMgr.read(ontModel, "etc/spin.ttl");
        RDFDataMgr.read(ontModel, "etc/spl.spin.ttl");
        RDFDataMgr.read(ontModel, "etc/foaf.owl");
    }

    @Test
    public void testArguments()
    {
        Resource argument = getOntModel().createResource("http://ontology/arg").addProperty(RDF.type, SPL.Argument).
                addProperty(SPL.predicate, FOAF.name);
        Resource template = getOntModel().createResource("http://ontology/template").addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, getOntModel().createResource().addProperty(RDF.type, SP.Construct).
                        addLiteral(SP.text, "CONSTRUCT WHERE { ?s ?p ?o }")).
                addProperty(SPIN.constraint, argument);
        Resource constraint = getOntModel().createResource("http://ontology/constraint").addProperty(RDF.type, template);

        assertEquals(argument.as(Argument.class), constraint.as(TemplateCall.class).getTemplate().getArguments(false).get(0));
    }

    public OntModel getOntModel()
    {
        return ontModel;
    }

}
