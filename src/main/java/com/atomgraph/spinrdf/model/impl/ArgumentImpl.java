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
package com.atomgraph.spinrdf.model.impl;

import com.atomgraph.spinrdf.model.Argument;
import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPL;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class ArgumentImpl extends ResourceImpl implements Argument
{

    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new ArgumentImpl(node, enhGraph);
            }
            else
            {
                throw new ConversionException("Cannot convert node " + node.toString() + " to Argument: it does not have rdf:type spl:Argument or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            if (eg.asGraph().contains(node, RDF.type.asNode(), SPL.Argument.asNode())) return true;
            else
                return (eg.asGraph().find(node, RDF.type.asNode(), null).
                    filterKeep(t -> eg.asGraph().contains(t.getObject(), RDFS.subClassOf.asNode(), SPL.Argument.asNode()))).
                    hasNext();
        }
        
    };
    
    public ArgumentImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }
        
    @Override
    public Integer getArgIndex()
    {
        String varName = getVarName();
        if(varName != null) {
            return SP.getArgPropertyIndex(varName);
        }
        else {
            return null;
        }
    }

    @Override
    public RDFNode getDefaultValue()
    {
        Statement s = getProperty(SPL.defaultValue);
        if(s != null) {
            return s.getObject();
        }
        else {
            return null;
        }
    }

    @Override
    public String getVarName()
    {
        Property argProperty = getPredicate();
        if(argProperty != null) {
            return argProperty.getLocalName();
        }
        else {
            return null;
        }
    }

    @Override
    public String getComment()
    {
        if (hasProperty(RDFS.comment) && getProperty(RDFS.comment).getObject().isLiteral())
            return getProperty(RDFS.comment).getString();
        
        return null;
    }

    @Override
    public Property getPredicate()
    {
        if (hasProperty(SPL.predicate) && getProperty(SPL.predicate).getObject().isURIResource())
            return new PropertyImpl(getProperty(SPL.predicate).getObject().asNode(), (EnhGraph)getProperty(SPL.predicate).getObject().getModel());

        return null;
    }

    @Override
    public Resource getValueType()
    {
        if (hasProperty(SPL.valueType) && getProperty(SPL.valueType).getObject().isResource())
            return getProperty(SPL.valueType).getResource();
        
        return null;
    }

    @Override
    public boolean isOptional()
    {
        Statement s = getProperty(SPL.optional);
        if(s != null && s.getObject().isLiteral()) {
            return s.getBoolean();
        }
        else {
            return false;
        }
    }
    
}
