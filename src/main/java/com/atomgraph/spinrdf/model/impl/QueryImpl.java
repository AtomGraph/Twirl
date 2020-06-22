/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model.impl;

import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.vocabulary.RDF;
import org.spinrdf.vocabulary.SP;

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
                throw new ConversionException( "Cannot convert node " + node.toString() + " to Query: it does not have rdf:type sp:Query or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            return eg.asGraph().contains(node, RDF.type.asNode(), SP.Query.asNode());
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
