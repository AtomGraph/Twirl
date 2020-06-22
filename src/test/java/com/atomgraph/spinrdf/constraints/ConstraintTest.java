/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.constraints;

import com.atomgraph.spinrdf.constraints.SPINConstraints.QueryWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
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
    private OntModel ontModel;

    @BeforeClass
    public static void init()
    {
        JenaSystem.init();
        com.atomgraph.spinrdf.vocabulary.SP.init(BuiltinPersonalities.model);
    }
    
    @Before
    public void ontology()
    {
        ontModel = ModelFactory.createOntologyModel(); // OntModelSpec.OWL_MEM
        ontModel.setDerivationLogging(true);
        
//        Resource query = ontModel.createResource().
//                addProperty(RDF.type, SP.Construct).
//                addLiteral(SP.text, "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
//"PREFIX  rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
//"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
//"PREFIX spin: <http://spinrdf.org/spin#>\n" +
//"\n" +
//        "CONSTRUCT {\n" +
//"    _:b0 a spin:ConstraintViolation .\n" +
//"    _:b0 spin:violationRoot ?this .\n" +
//"    _:b0 spin:violationPath ?predicate .\n" +
//"}\n" +
//"WHERE\n" +
//"  {\n" +
//"        { SELECT  (count(*) AS ?cardinality)\n" +
//"          WHERE\n" +
//"            { ?this  ?predicate  ?object \n" +
//"             # FILTER bound(?minCount) \n" +
//"            }\n" +
//"          HAVING ( ?cardinality < ?minCount )\n" +
//"        }\n" +
//"    UNION\n" +
//"        { SELECT  (count(*) AS ?cardinality)\n" +
//"          WHERE\n" +
//"            { ?this  ?predicate  ?object \n" +
//"               FILTER bound(?maxCount) \n" +
//"            }\n" +
//"          HAVING ( ?cardinality > ?maxCount )\n" +
//"        }\n" +
//"    UNION\n" +
//"      { ?this  ?predicate  ?object\n" +
//"            FILTER ( isURI(?object) || isBlank(?object) )\n" +
//"            FILTER bound(?valueType)\n" +
//"            FILTER NOT EXISTS {\n" +
//"                                ?object a ?class .\n" +
//"                                ?class (rdfs:subClassOf)* ?valueType\n" +
//"                              }\n" +
//"          }\n" +
//"    UNION\n" +
//"      { ?this  ?predicate  ?object\n" +
//"            FILTER isLiteral(?object)\n" +
//"            FILTER bound(?valueType)\n" +
//"            BIND(datatype(?object) AS ?datatype)\n" +
//"            FILTER ( ! ( ?datatype IN (?valueType, rdfs:Literal) || ( ( ! bound(?datatype) || ?datatype = rdf:langString ) && ?valueType = xsd:string ) ) )\n" +
//"      }\n" +
//"  }");
//
//        SPL.Attribute = ontModel.createResource("http://ontology/SPL.Attribute").
//                addProperty(RDF.type, SPIN.Template).
//                addProperty(SPIN.body, query);
        
        Ontology ontology = ontModel.createOntology("http://ontology/");
        ontology.addImport(ResourceFactory.createResource(SP.NS));
        ontology.addImport(ResourceFactory.createResource(SPIN.NS));
        ontology.addImport(ResourceFactory.createResource(SPL.NS));
        ontModel.loadImports();
        
        Resource templateConstraint = ontModel.createResource("http://ontology/bodyConstraint").
                addProperty(RDF.type, SPL.Attribute).
                addProperty(SPL.predicate, SPIN.body).
                addLiteral(SPL.minCount, 0).
                addLiteral(SPL.maxCount, 1);

        ontModel.createResource(SPIN.Template.getURI()).
                addProperty(SPIN.constraint, templateConstraint); // rdfs:subPropertyOf spin:query
        
//        final StmtIterator input = ontModel.listStatements(ontModel.createResource("http://ontology/bodyConstraint"), RDF.type, SP.Query);
//        assert( input.hasNext() );
//
//        final Iterator<Derivation> derivations = ((InfModel)ontModel).getDerivation(input.next());
//        assert( null != derivations );
//        assert( derivations.hasNext() );
//
//        while (derivations.hasNext())
//        {
//            System.out.println(derivations.next());
//        }
    }

    public List<ConstraintViolation> validate(Model model)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        Map<Resource, List<QueryWrapper>> class2Query = com.atomgraph.spinrdf.constraints.SPINConstraints.class2Query(ontModel);
        for (Resource cls : class2Query.keySet())
        {
            List<QueryWrapper> wrappers = class2Query.get(cls);
            for (QueryWrapper wrapper : wrappers)
                com.atomgraph.spinrdf.constraints.SPINConstraints.runQueryOnClass(cvs, wrapper, cls, model);
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
                addProperty(RDF.type, SPL.Attribute).
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
