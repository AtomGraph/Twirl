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
import org.apache.jena.ontapi.OntModelFactory;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.model.OntModel;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Exercises the SPIN model polymorphism ({@code as(Template)}/{@code as(Query)}) directly on an RDFS-inferred ontapi
 * {@link OntModel} whose personality carries the SPIN implementations (see {@link SpinSpecifications}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class InfOntModelCommandImplTest
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
    public void missingTemplateBody()
    {
        assertThrows(PropertyNotFoundException.class, () ->
                getOntModel().createResource().addProperty(RDF.type, SPIN.Template).as(Template.class).getBody()); // missing spin:body
    }

    @Test
    public void missingQueryText()
    {
        assertThrows(PropertyNotFoundException.class, () ->
                getOntModel().createResource().addProperty(RDF.type, SP.Construct).as(Query.class).getText()); // missing sp:text
    }

    public OntModel getOntModel()
    {
        return ontModel;
    }

}
