/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.util;

import java.util.ArrayList;
import java.util.List;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class JenaUtil
{
    
    public static List<Resource> getSuperClasses(Resource cls)
    {
        List<Resource> list = new ArrayList<>();
        list.add(cls);

        StmtIterator it = cls.listProperties(RDFS.subClassOf);
        
        try
        {
            while (it.hasNext()) list.addAll(getSuperClasses(it.next().getResource()));
        }
        finally
        {
            it.close();
        }
        
        return list;
    }
    
}
