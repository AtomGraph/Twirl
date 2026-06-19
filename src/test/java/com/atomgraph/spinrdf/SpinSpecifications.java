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
 */
package com.atomgraph.spinrdf;

import com.atomgraph.spinrdf.vocabulary.SP;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.ontapi.OntSpecification;
import org.apache.jena.ontapi.common.OntPersonality;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Derives a SPIN-aware ontapi {@link OntSpecification} by copying a standard spec's {@link OntPersonality}
 * (which extends {@link Personality}) and registering twirl's SPIN enhanced-node implementations into it via
 * {@link SP#init(Personality)}. Models created from the result resolve {@code canAs(Query)}/{@code canAs(TemplateCall)}
 * natively — demonstrating that the new ontapi personality carries the SPIN polymorphism, with no re-basing.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SpinSpecifications
{

    @SuppressWarnings("unchecked")
    public static OntSpecification spinAware(OntSpecification base)
    {
        OntPersonality personality = base.getPersonality().copy();
        SP.init((Personality<RDFNode>)personality); // OntPersonalityImpl extends Personality<RDFNode>
        return new OntSpecification(personality, base.getReasonerFactory());
    }

}
