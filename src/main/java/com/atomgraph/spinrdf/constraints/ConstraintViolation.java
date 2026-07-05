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

import com.atomgraph.spinrdf.model.TemplateCall;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import java.util.Collection;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;

/**
 * A violation of a SPIN constraint, produced when a constraint query matches an instance. It records the offending
 * root resource, the property paths and value involved, a human-readable message, the severity level and any
 * suggested fixes. Instances are turned into {@code spin:ConstraintViolation} triples by
 * {@link SPINConstraints#addConstraintViolationsRDF(java.util.List, org.apache.jena.rdf.model.Model, boolean)}.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ConstraintViolation
{

    private final Collection<TemplateCall> fixes;
    
    private Resource level;
    
    private final String message;
    
    private final Collection<SimplePropertyPath> paths;
    
    private final Resource root;
    
    private final Resource source;
    
    private RDFNode value;
    
    
    /**
     * Constructs a new ConstraintViolation.
     * @param root  the root resource of the violation
     * @param paths  the paths (may be empty)
     * @param fixes  potential fixes for the violations (may be empty)
     * @param message  the message explaining the error
     * @param source  the SPIN Query or template call that has caused this violation
     *                (may be null)
     */
    public ConstraintViolation(Resource root, 
                Collection<SimplePropertyPath> paths,
                Collection<TemplateCall> fixes,
                String message,
                Resource source)
    {
        this.fixes = fixes;
        this.message = message;
        this.root = root;
        this.paths = paths;
        this.source = source;
    }
    
    
    /**
     * Returns the suggested fixes for this violation.
     * @return the fixes (never {@code null}, may be empty)
     */
    public Collection<TemplateCall> getFixes()
    {
        return fixes;
    }


    /**
     * Returns the severity level of this violation.
     * @return the level, defaulting to {@link SPIN#Error} when none was set
     */
    public Resource getLevel()
    {
        return level == null ? SPIN.Error : level;
    }


    /**
     * Returns the human-readable message explaining this violation.
     * @return the message, or {@code null} if none was given
     */
    public String getMessage()
    {
        return message;
    }


    /**
     * Returns the property paths that this violation applies to.
     * @return the paths (never {@code null}, may be empty)
     */
    public Collection<SimplePropertyPath> getPaths()
    {
        return paths;
    }


    /**
     * Returns the root resource that violates the constraint.
     * @return the root resource
     */
    public Resource getRoot()
    {
        return root;
    }
    
    
    /**
     * Gets the SPIN Query or template call that has caused this violation.
     * @return the source (code should be robust against null values)
     */
    public Resource getSource()
    {
        return source;
    }
    
    
    /**
     * Returns the value that caused this violation.
     * @return the offending value, or {@code null} if not applicable
     */
    public RDFNode getValue()
    {
        return value;
    }
    

    /**
     * Checks if this represents an Error or Fatal.
     * @return true if Error or Fatal
     */
    public boolean isError()
    {
        return SPIN.Error.equals(getLevel()) || SPIN.Fatal.equals(getLevel());
    }
    
    
    /**
     * Sets the severity level of this violation.
     * @param level  the level (e.g. {@link SPIN#Error} or {@link SPIN#Fatal})
     */
    public void setLevel(Resource level)
    {
        this.level = level;
    }


    /**
     * Sets the value that caused this violation.
     * @param value  the offending value
     */
    public void setValue(RDFNode value)
    {
        this.value = value;
    }
        
    @Override
    public String toString()
    {
        return "Root: " + root + " Paths: " + paths + " " + (value != null ? value : "") + " Source: " + source;
    }
    
}
