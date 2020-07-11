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

import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class QueryImpl extends CommandImpl implements com.atomgraph.spinrdf.model.Query
{

    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new QueryImpl(node, enhGraph);
            }
            else
            {
                throw new ConversionException("Cannot convert node " + node.toString() + " to Query: it does not have rdf:type sp:Query or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            // Query instance
            if (eg.asGraph().contains(node, RDF.type.asNode(), SP.Query.asNode())) return true;
            else
            {
                // Query subclass instance
                if (eg.asGraph().find(node, RDF.type.asNode(), null).
                    filterKeep(t -> eg.asGraph().contains(t.getObject(), RDFS.subClassOf.asNode(), SP.Query.asNode())).
                    hasNext()) return true;
                else
                {
                    // Template instance
                    if (eg.asGraph().find(node, RDF.type.asNode(), null).
                        filterKeep(t -> eg.asGraph().contains(t.getObject(), RDF.type.asNode(), SPIN.Template.asNode())).
                        hasNext()) return true;
                    // Template subclass instance
                    else
                        return (eg.asGraph().find(node, RDF.type.asNode(), null).
                        filterKeep(t -> eg.asGraph().find(t.getObject(), RDF.type.asNode(), null).
                                filterKeep(tt ->  eg.asGraph().contains(tt.getObject(), RDFS.subClassOf.asNode(), SPIN.Template.asNode())).
                                hasNext()).
                        hasNext());
                }
            }
        }
        
    };
    
    public QueryImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }

    @Override
    public Query asQuery()
    {
        return QueryFactory.create(getText());
    }
    
}
