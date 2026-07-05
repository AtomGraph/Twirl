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

import org.apache.jena.query.QuerySolutionMap;

/**
 * An invocation of a SPIN {@link Template} with concrete argument values. A template call resolves to the
 * template's body query with its arguments bound to the supplied values.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public interface TemplateCall extends Command
{

    /**
     * Returns the template that this call invokes.
     * @return the template, or {@code null} if it cannot be resolved
     */
    Template getTemplate();

    /**
     * Returns the argument values of this call as a variable binding suitable for query execution.
     * @return the initial binding (never {@code null})
     */
    QuerySolutionMap getInitialBinding();

}
