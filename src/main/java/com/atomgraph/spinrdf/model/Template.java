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

import java.util.List;
import java.util.Map;
import org.apache.jena.rdf.model.Resource;

/**
 * A SPIN template: a reusable, parameterized SPARQL query or update identified by an {@code spin:Template}
 * resource. A template declares {@link Argument}s and holds a query body into which argument values are bound.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public interface Template extends Resource
{

    /**
     * Returns the arguments declared by this template.
     * @param ordered  if {@code true}, the arguments are sorted by their index
     * @return the list of arguments (never {@code null})
     */
    List<Argument> getArguments(boolean ordered);

    /**
     * Returns the declared arguments keyed by their variable name.
     * @return a map from variable name to argument (never {@code null})
     */
    Map<String, Argument> getArgumentsMap();

    /**
     * Returns the query or update that forms the body of this template.
     * @return the template body, or {@code null} if none is declared
     */
    Query getBody();

}
