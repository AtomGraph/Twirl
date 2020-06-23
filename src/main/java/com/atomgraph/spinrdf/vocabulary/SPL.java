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
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class SPL
{
    
    public final static String BASE_URI = "http://spinrdf.org/spl";
    
    public final static String NS = BASE_URI + "#";
    
    public final static String PREFIX = "spl";
    

    public final static Resource Argument = ResourceFactory.createResource(NS + "Argument");
    
    public final static Resource Attribute = ResourceFactory.createResource(NS + "Attribute");
    
    public final static Property defaultValue = ResourceFactory.createProperty(NS + "defaultValue");
   
    public final static Property hasValue = ResourceFactory.createProperty(NS + "hasValue");
    
    public final static Property maxCount = ResourceFactory.createProperty(NS + "maxCount");
    
    public final static Property minCount = ResourceFactory.createProperty(NS + "minCount");
    
    public final static Property optional = ResourceFactory.createProperty(NS + "optional");
    
    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
    
    public final static Property valueType = ResourceFactory.createProperty(NS + "valueType");
        
}
