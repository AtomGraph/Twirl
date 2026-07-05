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
 * A reverse property path step: from the root resource back along the predicate to its subjects
 * ({@code ?subject predicate root}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SubjectPropertyPath extends SimplePropertyPath
{

    /**
     * Constructs a reverse property path step.
     * @param root  the resource the path starts from
     * @param predicate  the predicate traversed back towards its subjects
     */
    public SubjectPropertyPath(Resource root, Property predicate)
    {
        super(root, predicate);
    }

}
