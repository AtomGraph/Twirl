/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.spinrdf.constraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.spinrdf.vocabulary.SP;
import org.spinrdf.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ConstraintTest
{
    private static Model ontModel;
    
    @BeforeClass
    public static void ontology()
    {
        ontModel = ModelFactory.createDefaultModel();
        
        Resource query = ontModel.createResource().
                addProperty(RDF.type, SP.Ask).
                addLiteral(SP.text, "SELECT *\n" +
"WHERE\n" +
"  { SELECT  (count(*) AS ?cardinality)\n" +
"    WHERE\n" +
"      { ?this  ?predicate  ?object }\n" +
"    HAVING ( ?cardinality != ?count )\n" +
"  }");

        Resource cardinalityTemplate = ontModel.createResource("http://ontology/cardinalityTemplate").
                addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, query);
        
        Resource templateConstraint = ontModel.createResource("http://ontology/bodyConstraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(ResourceFactory.createProperty("http://ontology/predicate"), SPIN.body).
                addLiteral(ResourceFactory.createProperty("http://ontology/count"), 1);

        ontModel.createResource(SPIN.Template.getURI()).
                addProperty(SPIN.constraint, templateConstraint);

        
        Resource clsConstraint = ontModel.createResource("http://ontology/nameConstraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(ResourceFactory.createProperty("http://ontology/predicate"), FOAF.name).
                addLiteral(ResourceFactory.createProperty("http://ontology/count"), 1);

        Resource cls = ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, clsConstraint);
    }

    public Map<Resource, Query> class2Query(Model model)
    {
        Map<Resource, Query> class2Query = new HashMap<>();
        StmtIterator constraintIt = model.listStatements((Resource)null, SPIN.constraint, (Resource)null);
        while (constraintIt.hasNext())
        {
            Statement stmt = constraintIt.next();
            
            Resource constrainedClass = stmt.getSubject();
            Resource constraint = stmt.getResource();
            Resource constraintType = constraint.getPropertyResourceValue(RDF.type);
            Resource constraintBody = constraintType.getPropertyResourceValue(SPIN.body);
            String constraintQueryString = constraintBody.getProperty(SP.text).getString();
            Query constraintQuery = QueryFactory.create(constraintQueryString);
        
            class2Query.put(constrainedClass, constraintQuery);
            //System.out.println(class2Query);
            // SPIN template. TO-DO: SPIN query
        }
        constraintIt.close();
        return class2Query;
    }
    
    public void runQueryOnClass(List<ConstraintViolation> cvs, Query query, Resource cls, Model model)
    {
        QuerySolutionMap qsm = new QuerySolutionMap();

        Resource constraint = cls.getPropertyResourceValue(SPIN.constraint);
        StmtIterator constraintProps = constraint.listProperties();
        Property property = null;
        while (constraintProps.hasNext())
        {
            Statement stmt = constraintProps.next();
            property = stmt.getObject().as(Property.class);
            if (!stmt.getPredicate().equals(RDF.type)) qsm.add(stmt.getPredicate().getLocalName(), property);
        }
        constraintProps.close();
            
        ResIterator it = model.listSubjectsWithProperty(RDF.type, cls);
        while (it.hasNext())
        {
            Resource instance = it.next();

            qsm.add(SPIN.THIS_VAR_NAME, instance);
            System.out.println(qsm);
          
            try (QueryExecution qex = QueryExecutionFactory.create(query, model, qsm))
            {
                ResultSet rs = qex.execSelect();
                
                while (rs.hasNext())
                {
                    QuerySolution qs = rs.next();
                    
                    List<SimplePropertyPath> paths = new ArrayList<>();
                    paths.add(new ObjectPropertyPath(instance, property));
                    cvs.add(new ConstraintViolation(instance, paths, null, "Violation failed", null));
                }
            }
        }
        
        it.close();
    }


    @Test
    public void testValidateSystemCardinality()
    {
        List<ConstraintViolation> cvs = new ArrayList<>();

        Map<Resource, Query> class2Query = class2Query(ontModel);
        for (Resource cls : class2Query.keySet())
        {
            Query query = class2Query.get(cls);
            
            runQueryOnClass(cvs, query, cls, ontModel);
        }
        
        assertEquals(0, cvs.size());
    }
    
    @Test
    public void testValidateCardinality()
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        Model model = ModelFactory.createDefaultModel();
        Resource violatingInstance = model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class"));
                // addLiteral(FOAF.name, "Shit"); // missing foaf:name validates constraint
        
        Map<Resource, Query> class2Query = class2Query(ontModel);
        for (Resource cls : class2Query.keySet())
        {
            Query query = class2Query.get(cls);

            runQueryOnClass(cvs, query, cls, model);
        }
        
        assertEquals(1, cvs.size());
    }
    
}
