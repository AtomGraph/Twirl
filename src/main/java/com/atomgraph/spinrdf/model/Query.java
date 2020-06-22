/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.model;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public interface Query extends Command
{
    
    org.apache.jena.query.Query asQuery();
    
}
