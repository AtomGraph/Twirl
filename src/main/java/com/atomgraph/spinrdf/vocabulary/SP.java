/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.vocabulary;

import com.atomgraph.spinrdf.model.Argument;
import com.atomgraph.spinrdf.model.Query;
import com.atomgraph.spinrdf.model.Template;
import com.atomgraph.spinrdf.model.impl.ArgumentImpl;
import com.atomgraph.spinrdf.model.impl.QueryImpl;
import com.atomgraph.spinrdf.model.impl.TemplateImpl;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class SP
{
    
    public final static String BASE_URI = "http://spinrdf.org/sp";

    public final static String NS = BASE_URI + "#";
    
    public final static Resource Query = ResourceFactory.createResource(NS + "Query");

    static
    {
        SP.init(BuiltinPersonalities.model);
    }
    
    public static void init(Personality<RDFNode> p)
    {
        p.add(Argument.class, ArgumentImpl.factory);
        p.add(Query.class, QueryImpl.factory);
        p.add(Template.class, TemplateImpl.factory);
    }
    
}
