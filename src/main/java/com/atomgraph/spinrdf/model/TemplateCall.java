/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model;

import org.apache.jena.query.QuerySolutionMap;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public interface TemplateCall extends Command
{
    
    Template getTemplate();
    
    QuerySolutionMap getInitialBinding();

}
