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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * A single step of a property path rooted at a resource, used to describe where a constraint violation applies.
 * Concrete subclasses distinguish the direction of the step: {@link ObjectPropertyPath} (forward) and
 * {@link SubjectPropertyPath} (reverse).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public abstract class SimplePropertyPath
{

    private final Property predicate;
    private final Resource root;

    /**
     * Constructs a property path step.
     * @param root  the resource the path starts from
     * @param predicate  the predicate traversed by this step
     */
    public SimplePropertyPath(Resource root, Property predicate)
    {
        this.predicate = predicate;
        this.root = root;
    }

    /**
     * Returns the predicate traversed by this path step.
     * @return the predicate
     */
    public Property getPredicate()
    {
        return predicate;
    }

    /**
     * Returns the resource this path step starts from.
     * @return the root resource
     */
    public Resource getRoot()
    {
        return root;
    }
    
    @Override
    public String toString()
    {
        return root + " " + predicate;
    }
        
}
