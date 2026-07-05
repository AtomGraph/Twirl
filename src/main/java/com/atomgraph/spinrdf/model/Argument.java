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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A declared argument (parameter) of a SPIN template or function, backed by an {@code spl:Argument} resource.
 * An argument binds a predicate to a value type and may be optional, carry a default value and a comment.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public interface Argument extends Resource
{

    /**
     * Returns the predicate that this argument is bound to.
     * @return the argument predicate, or {@code null} if none is declared
     */
    Property getPredicate();

    /**
     * Returns the type that values of this argument are expected to have.
     * @return the value type, or {@code null} if unconstrained
     */
    Resource getValueType();

    /**
     * Indicates whether this argument may be omitted.
     * @return {@code true} if the argument is optional
     */
    boolean isOptional();

    /**
     * Returns the default value used when no value is supplied for this argument.
     * @return the default value, or {@code null} if none is declared
     */
    RDFNode getDefaultValue();

    /**
     * Returns the name of the SPARQL variable bound to this argument (the predicate's local name).
     * @return the variable name
     */
    String getVarName();

    /**
     * Returns the 1-based position of this argument, derived from an {@code argN} predicate.
     * @return the argument index, or {@code null} if it cannot be determined
     */
    Integer getArgIndex();

    /**
     * Returns the human-readable comment describing this argument.
     * @return the comment, or {@code null} if none is declared
     */
    String getComment();

}
