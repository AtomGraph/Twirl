/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model.impl;

import com.atomgraph.spinrdf.vocabulary.SPIN;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class TemplateCallImpl extends CommandImpl
{
    
    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new TemplateCallImpl(node, enhGraph);
            }
            else
            {
                throw new ConversionException("Cannot convert node " + node.toString() + " to TemplateCall: it does not have rdf:type with TemplateCall or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            ExtendedIterator<Triple> it = eg.asGraph().find(node, RDF.type.asNode(), null);
            try
            {
                while (it.hasNext())
                {
                    Triple triple = it.next();
                    if (eg.asGraph().contains(triple.getObject(), RDF.type.asNode(), SPIN.Template.asNode())) return true;
                }
            }
            finally
            {
                it.close();
            }
            
            return false;
        }
        
    };

    public TemplateCallImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }
    
    @Override
    public String getText()
    {
        return getTemplate().getBody().getText();
    }
    
}
