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

import com.atomgraph.spinrdf.vocabulary.SPIN;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class PlainOntModelConstraintTest extends InfOntModelConstraintTest
{
    
    @Override
    public OntModel createOntModel()
    {
        return ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
    }
    
    @Test
    @Override
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
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }

    @Test
    @Override
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
        assertEquals(1, SPINConstraints.check(getOntModel()).size());
    }
    
}
