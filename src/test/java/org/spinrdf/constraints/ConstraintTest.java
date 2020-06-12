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
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.spinrdf.vocabulary.SP;
import org.spinrdf.vocabulary.SPIN;
import org.spinrdf.vocabulary.SPL;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class ConstraintTest
{
    private Model ontModel;
    private Resource cardinalityTemplate;
    
    public class QueryWrapper
    {
        private final Query query;
        private final QuerySolutionMap qsm;
        
        public QueryWrapper(Query query, QuerySolutionMap qsm)
        {
            this.query = query;
            this.qsm = qsm;
        }
        
        public Query getQuery()
        {
            return query;
        }
        
        public QuerySolutionMap getQuerySolutionMap()
        {
            return qsm;
        }
        
    }
    
    @Before
    public void ontology()
    {
        ontModel = ModelFactory.createDefaultModel();
        
        Resource query = ontModel.createResource().
                addProperty(RDF.type, SP.Ask).
                addLiteral(SP.text, "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
"\n" +
"SELECT *\n" +
"WHERE\n" +
"  {   { FILTER bound(?minCount)\n" +
"        { SELECT  (count(*) AS ?cardinality)\n" +
"          WHERE\n" +
"            { ?this  ?predicate  ?object }\n" +
"          HAVING ( ?cardinality < ?minCount )\n" +
"        }\n" +
"      }\n" +
"    UNION\n" +
"      { FILTER bound(?maxCount)\n" +
"        { SELECT  (count(*) AS ?cardinality)\n" +
"          WHERE\n" +
"            { ?this  ?predicate  ?object }\n" +
"          HAVING ( ?cardinality > ?maxCount )\n" +
"        }\n" +
"      }\n" +
"    UNION\n" +
"      { ?this  ?predicate  ?object\n" +
"            FILTER ( isURI(?object) || isBlank(?object) )\n" +
"            FILTER bound(?valueType)\n" +
"            FILTER NOT EXISTS {\n" +
"                                ?object a ?class .\n" +
"                                ?class (rdfs:subClassOf)* ?valueType\n" +
"                              }\n" +
"          }\n" +
"    UNION\n" +
"      { ?this  ?predicate  ?object\n" +
"            FILTER isLiteral(?object)\n" +
"            FILTER bound(?valueType)\n" +
"            BIND(datatype(?object) AS ?datatype)\n" +
"            FILTER ( ! ( ?datatype IN (?valueType, rdfs:Literal) || ( ( ! bound(?datatype) || ?datatype = rdf:langString ) && ?valueType = xsd:string ) ) )\n" +
"      }\n" +
"  }");

        cardinalityTemplate = ontModel.createResource("http://ontology/cardinalityTemplate").
                addProperty(RDF.type, SPIN.Template).
                addProperty(SPIN.body, query);
        
        Resource templateConstraint = ontModel.createResource("http://ontology/bodyConstraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, SPIN.body).
                addLiteral(SPL.minCount, 0).
                addLiteral(SPL.maxCount, 1);

        ontModel.createResource(SPIN.Template.getURI()).
                addProperty(SPIN.constraint, templateConstraint);
    }

    public Map<Resource, List<QueryWrapper>> class2Query(Model model)
    {
        Map<Resource, List<QueryWrapper>> class2Query = new HashMap<>();
                
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
        
            QuerySolutionMap qsm = new QuerySolutionMap();
            StmtIterator constraintProps = constraint.listProperties();
            Property property = null;
            while (constraintProps.hasNext())
            {
                Statement propStmt = constraintProps.next();
                property = propStmt.getObject().as(Property.class);
                if (!propStmt.getPredicate().equals(RDF.type)) qsm.add(propStmt.getPredicate().getLocalName(), property);
            }
            constraintProps.close();
        
            QueryWrapper wrapper = new QueryWrapper(constraintQuery, qsm);
            
            if (class2Query.containsKey(constrainedClass))
                class2Query.get(constrainedClass).add(wrapper);
            else
            {
                List<QueryWrapper> wrapperList = new ArrayList<>();
                wrapperList.add(wrapper);
                class2Query.put(constrainedClass, wrapperList);
            }
            
            //System.out.println(class2Query);
            // SPIN template. TO-DO: SPIN query
        }
        constraintIt.close();
        
        return class2Query;
    }
    
    public void runQueryOnClass(List<ConstraintViolation> cvs, QueryWrapper wrapper, Resource cls, Model model)
    {
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.addAll(wrapper.getQuerySolutionMap());

        ResIterator it = model.listSubjectsWithProperty(RDF.type, cls);
        while (it.hasNext())
        {
            Resource instance = it.next();

            qsm.add(SPIN.THIS_VAR_NAME, instance);
            System.out.println(qsm);
          
            try (QueryExecution qex = QueryExecutionFactory.create(wrapper.getQuery(), model, qsm))
            {
                ResultSet rs = qex.execSelect();
//                ResultSetFormatter.out(System.out, rs);
                
                while (rs.hasNext())
                {
                    QuerySolution qs = rs.next();
                    
                    List<SimplePropertyPath> paths = new ArrayList<>();
                    paths.add(new ObjectPropertyPath(instance, null)); // property
                    cvs.add(new ConstraintViolation(instance, paths, null, "Validation failed", null));
                }
            }
        }
        
        it.close();
    }

    public List<ConstraintViolation> validate(Model model)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        Map<Resource, List<QueryWrapper>> class2Query = class2Query(ontModel);
        for (Resource cls : class2Query.keySet())
        {
            List<QueryWrapper> wrappers = class2Query.get(cls);
            for (QueryWrapper wrapper : wrappers)
                runQueryOnClass(cvs, wrapper, cls, model);
        }
        
        return cvs;
    }
    
    @Test
    public void validateSystem()
    {
        assertEquals(0, validate(ontModel).size());
    }
    
    @Test
    public void invalidMinCount()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.minCount, 1);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class"));
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void invalidMaxCount()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, FOAF.name).
                addLiteral(SPL.maxCount, 1);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "one").
                addLiteral(FOAF.name, "two");
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void invalidResourceValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource notConcept = model.createResource("http://ontology/not-skos-concept").
                addProperty(RDF.type, SKOS.Collection);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, notConcept);
        
        assertEquals(1, validate(model).size());
    }

    @Test
    public void invalidResourceSubClassValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource subClass = model.createResource("http://data/not-concept-subclass").
                addProperty(RDFS.subClassOf, SKOS.Collection);
        Resource notConcept = model.createResource("http://ontology/not-skos-concept").
                addProperty(RDF.type, subClass);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, notConcept);
        
        assertEquals(1, validate(model).size());
    }
    
    @Test
    public void validResourceValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource concept = model.createResource("http://data/concept").
                addProperty(RDF.type, SKOS.Concept);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, concept);
        
        assertEquals(0, validate(model).size());
    }

    @Test
    public void validResourceSubClassValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, SKOS.Concept);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        Resource subClass = model.createResource("http://data/concept-subclass").
                addProperty(RDFS.subClassOf, SKOS.Concept);
        Resource concept = model.createResource("http://data/concept").
                addProperty(RDF.type, subClass);
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(DCTerms.subject, concept);
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void validLiteralValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, DCTerms.subject).
                addProperty(SPL.valueType, RDFS.Literal);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint).
                addProperty(FOAF.name, SKOS.Collection); // will not cause violation since isLiteral(?object) = false
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addProperty(FOAF.name, "literal");
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void validStringValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, "literal");
        
        assertEquals(0, validate(model).size());
    }
    
    @Test
    public void invalidStringValueType()
    {
        Resource constraint = ontModel.createResource("http://ontology/constraint").
                addProperty(RDF.type, cardinalityTemplate).
                addProperty(SPL.predicate, FOAF.name).
                addProperty(SPL.valueType, XSD.xstring);
        ontModel.createResource("http://ontology/class").
                addProperty(RDF.type, RDFS.Class).
                addProperty(SPIN.constraint, constraint);
        
        Model model = ModelFactory.createDefaultModel();
        model.createResource("http://data/instance").
                addProperty(RDF.type, model.createResource("http://ontology/class")).
                addLiteral(FOAF.name, 42);

        assertEquals(1, validate(model).size());
    }
    
}
