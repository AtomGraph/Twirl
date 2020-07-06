package com.atomgraph.spinrdf.jena;

import java.util.HashMap;
import java.util.Map;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.resultset.ResultSetCompare;
import org.apache.jena.sparql.syntax.syntaxtransform.QueryTransformOps;
import org.apache.jena.sparql.vocabulary.FOAF;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class TransformVsQSMTest
{
    
    @Test
    public void testBGP()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
        Query query = QueryFactory.create("SELECT ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(query, model, qsm).execSelect();
//        ResultSetFormatter.out(System.out, qsmRs);

        Map<String, RDFNode> subs = new HashMap<>();
        subs.put("predicate", FOAF.maker);
        query = QueryTransformOps.transformQuery(query, subs);
        ResultSet transfomRs = QueryExecutionFactory.create(query, model).execSelect();
//        ResultSetFormatter.out(System.out, transfomRs);

        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, transfomRs));
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
        
        Query qsmQuery = QueryFactory.create("SELECT ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"       FILTER bound(?maxCount)\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
//        ResultSetFormatter.out(System.out, qsmRs);

        Map<String, RDFNode> subs = new HashMap<>();
        subs.put("predicate", FOAF.maker);
        subs.put("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        
        Query transformQuery = QueryFactory.create("SELECT ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"       BIND(?maxCount AS ?mc) FILTER bound(?mc)\n" +
"  }"); 
        transformQuery = QueryTransformOps.transformQuery(transformQuery, subs);
        ResultSet transformRs = QueryExecutionFactory.create(transformQuery, model).execSelect();
//        ResultSetFormatter.out(System.out, transformRs);
        
        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, transformRs));
    }
    
    @Test
    public void testFilterUnbound()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
//        qsm.add("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        
        Query qsmQuery = QueryFactory.create("SELECT ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"       FILTER bound(?maxCount)\n" +
"  }"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
//        ResultSetFormatter.out(System.out, qsmRs);

        Map<String, RDFNode> subs = new HashMap<>();
        subs.put("predicate", FOAF.maker);
//        subs.put("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        
        Query transformQuery = QueryFactory.create("SELECT ?value\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"       BIND(?maxCount AS ?mc) FILTER bound(?mc)\n" +
"  }"); 
        transformQuery = QueryTransformOps.transformQuery(transformQuery, subs);
        ResultSet transformRs = QueryExecutionFactory.create(transformQuery, model).execSelect();
//        ResultSetFormatter.out(System.out, transformRs);
        
        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, transformRs));
    }
    
    @Test
    public void testHavingSet()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker1")).
                addProperty(FOAF.maker, model.createResource("http://data/maker2"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
        qsm.add("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Query qsmQuery = QueryFactory.create("SELECT (count(*) AS ?cardinality)\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }\n" +
"HAVING ( ?cardinality < ?maxCount )"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
        ResultSetFormatter.out(System.out, qsmRs);

        Map<String, RDFNode> subs = new HashMap<>();
        subs.put("predicate", FOAF.maker);
        subs.put("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Query transformQuery = QueryFactory.create("SELECT (count(*) AS ?cardinality)\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }\n" +
"HAVING ( ?cardinality < ?maxCount )");
        transformQuery = QueryTransformOps.transformQuery(transformQuery, subs);
        ResultSet transfomRs = QueryExecutionFactory.create(transformQuery, model).execSelect();
        ResultSetFormatter.out(System.out, transfomRs);

        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, transfomRs));
    }

    @Test
    public void testHavingUnset()
    {
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(FOAF.maker, model.createResource("http://data/maker1")).
                addProperty(FOAF.maker, model.createResource("http://data/maker2"));
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("predicate", FOAF.maker);
//        qsm.add("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Query qsmQuery = QueryFactory.create("SELECT (count(*) AS ?cardinality)\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }\n" +
"HAVING ( ?cardinality < ?maxCount )"); 
        ResultSet qsmRs = QueryExecutionFactory.create(qsmQuery, model, qsm).execSelect();
        ResultSetFormatter.out(System.out, qsmRs);

        Map<String, RDFNode> subs = new HashMap<>();
        subs.put("predicate", FOAF.maker);
//        subs.put("maxCount", ResourceFactory.createTypedLiteral("1", XSDDatatype.XSDinteger));
        Query transformQuery = QueryFactory.create("SELECT (count(*) AS ?cardinality)\n" +
"WHERE\n" +
"  {   <http://data/instance> ?predicate  ?value\n" +
"  }\n" +
"HAVING ( ?cardinality < ?maxCount )");
        transformQuery = QueryTransformOps.transformQuery(transformQuery, subs);
        ResultSet transfomRs = QueryExecutionFactory.create(transformQuery, model).execSelect();
        ResultSetFormatter.out(System.out, transfomRs);

        assertTrue(ResultSetCompare.equalsByTerm(qsmRs, transfomRs));
    }
    
}
