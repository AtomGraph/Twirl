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
package com.atomgraph.spinrdf.vocabulary;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Constants for the SPL (SPIN standard library) vocabulary, which defines reusable functions, attribute
 * definitions and constraint templates. Terms are defined by the
 * <a href="http://spinrdf.org/spl">SPL namespace</a> ({@code http://spinrdf.org/spl#}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SPL
{

    private SPL() { }

    /** Base URI of the namespace: {@value}. */
    public final static String BASE_URI = "http://spinrdf.org/spl";
    
    /** Namespace URI: {@value}. */
    public final static String NS = BASE_URI + "#";
    
    /** Recommended namespace prefix: {@value}. */
    public final static String PREFIX = "spl";
    

    /** {@code spl:Argument} */
    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
    /** {@code spl:Attribute} */
    public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
    /** {@code spl:defaultValue} */
    public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");
   
    /** {@code spl:hasValue} */
    public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
    
    /** {@code spl:maxCount} */
    public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
    
    /** {@code spl:minCount} */
    public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    /** {@code spl:optional} */
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
    /** {@code spl:predicate} */
    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
    
    /** {@code spl:valueType} */
    public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
        
}
