/*
 * Copyright 2026 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atomgraph.spinrdf.model;

import org.apache.jena.shared.JenaException;

/**
 * Thrown when an RDF node cannot be viewed as a SPIN model type (e.g. a constraint that is neither a
 * {@link Query} nor a {@link TemplateCall}).
 *
 * Replaces the legacy {@code org.apache.jena.ontology.ConversionException} so that this SPIN library carries no
 * dependency on the deprecated Jena ontology API.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ConversionException extends JenaException
{

    public ConversionException(String message)
    {
        super(message);
    }

}
