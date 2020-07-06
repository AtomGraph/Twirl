/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.jena;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.vocabulary.FOAF;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ValuesVsQSMTest
{
    
    @Test
    public void testBGP()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
        Query qsmQuery = QueryFactory.create("SELECT *\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();

        Query valuesQuery = QueryFactory.create("SELECT *\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }\n" +
"VALUES ( ?predicate ) {\n" +
"  ( <http://xmlns.com/foaf/0.1/maker> )\n" +
"}");
        ResultSet valuesRs = QueryExecutionFactory.create(valuesQuery, model).execSelect();

        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, valuesRs));
    }
    
    @Test
    public void testFilterBound()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
        qsm.add("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        
        Query qsmQuery = QueryFactory.create("SELECT *\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"      FILTER bound(?maxCount)\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
//        ResultSetFormatter.out(System.out, qsmRs);

        Query valuesQuery = QueryFactory.create("SELECT ?predicate ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  } \n" +
"\nVALUES ( ?predicate ?maxCount) {\n" +
"  ( <http://xmlns.com/foaf/0.1/maker> 1)\n" +
"}");
//        System.out.println(valuesQuery);
        ResultSet valuesRs = QueryExecutionFactory.create(valuesQuery, model).execSelect();
//        ResultSetFormatter.out(System.out, valuesRs);
        
        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, valuesRs));
    }
    
    @Test
    public void testFilterUnbound()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
        
        Query qsmQuery = QueryFactory.create("SELECT *\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"      FILTER bound(?maxCount)\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
        ResultSetFormatter.out(System.out, qsmRs);

        Query valuesQuery = QueryFactory.create("SELECT ?predicate ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  } \n" +
"\nVALUES ( ?predicate ?maxCount ) {\n" +
"  ( <http://xmlns.com/foaf/0.1/maker> UNDEF)\n" +
"}");
        ResultSet valuesRs = QueryExecutionFactory.create(valuesQuery, model).execSelect();
        ResultSetFormatter.out(System.out, valuesRs);
        
        //assertTrue(ResultSetCompare.equalsExact(qsmRs, valuesRs));
    }
}
