/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model.impl;

import com.atomgraph.spinrdf.model.Argument;
import com.atomgraph.spinrdf.model.Query;
import com.atomgraph.spinrdf.model.Template;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.impl.OntClassImpl;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class TemplateImpl extends OntClassImpl implements Template
{

    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new TemplateImpl(node, enhGraph);
            }
            else
            {
                throw new ConversionException( "Cannot convert node " + node.toString() + " to Template: it does not have rdf:type spin:Template or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            return (eg.asGraph().contains(node, RDF.type.asNode(), SPIN.Template.asNode()));
        }
        
    };
    
    public TemplateImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }
    
    @Override
    public Query getBody()
    {
        return getRequiredProperty(SPIN.body).getResource().as(Query.class);
    }

    @Override
    public List<Argument> getArguments(boolean ordered)
    {
        List<Argument> results = new ArrayList<>();
        StmtIterator it = null;
        try {
            Set<OntClass> classes = listSuperClasses().toSet(); // JenaUtil.getAllSuperClasses(this);
            classes.add(this);
            for(Resource cls : classes) {
                it = cls.listProperties(SPIN.constraint);
                while(it.hasNext()) {
                    Statement s = it.nextStatement();
                    addArgumentFromConstraint(s, results);
                }
            }
        }
        finally {
            if (it != null) {
                it.close();
            }
        }
        
        if(ordered) {
            Collections.sort(results, new Comparator<Argument>() {
                @Override
                public int compare(    Argument o1,     Argument o2) {
                    Property p1 = o1.getPredicate();
                    Property p2 = o2.getPredicate();
                    if(p1 != null && p2 != null) {
                        return p1.getLocalName().compareTo(p2.getLocalName());
                    }
                    else {
                        return 0;
                    }
                }
            });
        }
        
        return results;
    }

    /**
     * 
     * @param constraint is a statement whose subject is a class, and whose predicate is SPIN.constraint
     * @param results
     */
    private void addArgumentFromConstraint(Statement constraint, List<Argument> results) {
        if (constraint.getResource().canAs(Argument.class)) results.add(constraint.getResource().as(Argument.class));
    }
        
    @Override
    public Map<String, Argument> getArgumentsMap()
    {
        Map<String,    Argument> results = new HashMap<>();
        
        for (Argument argument : getArguments(false))
        {
            Property property = argument.getPredicate();
            if(property != null) {
                results.put(property.getLocalName(), argument);
            }
        }
        
        return results;
    }
    
}
