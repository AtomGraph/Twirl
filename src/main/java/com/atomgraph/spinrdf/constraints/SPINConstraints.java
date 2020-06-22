/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.spinrdf.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.spinrdf.constraints.ObjectPropertyPath;
import org.spinrdf.constraints.SimplePropertyPath;
import org.spinrdf.constraints.SubjectPropertyPath;
import com.atomgraph.spinrdf.model.TemplateCall;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSetFormatter;
import org.spinrdf.system.SPINLabels;
import org.spinrdf.util.JenaUtil;
import org.spinrdf.vocabulary.SP;
import org.spinrdf.vocabulary.SPIN;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class SPINConstraints
{
    
    public static class QueryWrapper
    {
        
        private final Resource source;
        private final Query query;
        private final QuerySolutionMap qsm;
        
        public QueryWrapper(Resource source, Query query, QuerySolutionMap qsm)
        {
            this.source = source;
            this.query = query;
            this.qsm = qsm;
        }
        
        public Resource getSource()
        {
            return source;
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
    
    public static Map<Resource, List<QueryWrapper>> class2Query(OntModel model)
    {
        Map<Resource, List<QueryWrapper>> class2Query = new HashMap<>();
                
        StmtIterator constraintIt = model.listStatements((Resource)null, SPIN.constraint, (Resource)null);
        while (constraintIt.hasNext())
        {
            Statement stmt = constraintIt.next();
            
            Resource constrainedClass = stmt.getSubject();
            Resource constraint = stmt.getResource();
            final Query constraintQuery;
            
            //if (constraint.hasProperty(SP.text)) // SPIN query
            if (constraint.canAs(com.atomgraph.spinrdf.model.Query.class))
            {
                com.atomgraph.spinrdf.model.Query query = constraint.as(com.atomgraph.spinrdf.model.Query.class);
                constraintQuery = QueryFactory.create(query.getText());
            }
            else // SPIN query template
            {
                //Resource constraintType = constraint.getPropertyResourceValue(RDF.type);
                
                TemplateCall templateCall = constraint.as(TemplateCall.class);
                
//                Resource constraintBody = constraintType.getPropertyResourceValue(SPIN.body);
//                String constraintQueryString = constraintBody.getProperty(SP.text).getString();
                constraintQuery = QueryFactory.create(templateCall.getTemplate().getBody().getText());
            }

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
        
            QueryWrapper wrapper = new QueryWrapper(constraint, constraintQuery, qsm);
            
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
    
    public static void runQueryOnClass(List<ConstraintViolation> cvs, QueryWrapper wrapper, Resource cls, Model model)
    {
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.addAll(wrapper.getQuerySolutionMap());

        Model violationModel = ModelFactory.createDefaultModel();
        
        ResIterator it = model.listSubjectsWithProperty(RDF.type, cls);
        while (it.hasNext())
        {
            Resource instance = it.next();

            qsm.add(SPIN.THIS_VAR_NAME, instance);
            System.out.println(qsm);
          
            try (QueryExecution qex = QueryExecutionFactory.create(wrapper.getQuery(), model, qsm))
            {
                qex.execConstruct(violationModel);
                //ResultSetFormatter.out(System.out, qex.execSelect());

                addConstructedProblemReports(violationModel, cvs, model, cls, null, null, wrapper.getSource());
            }
        }
        
        it.close();
    }

    private static void addConstructedProblemReports(
            Model cm,
            List<ConstraintViolation> results,
            Model model,
            Resource atClass,
            Resource matchRoot,
            String label,
            Resource source) {
        StmtIterator it = cm.listStatements(null, RDF.type, SPIN.ConstraintViolation);
        while(it.hasNext()) {
            Statement s = it.nextStatement();
            Resource vio = s.getSubject();
            
            Resource root = null;
            Statement rootS = vio.getProperty(SPIN.violationRoot);
            if(rootS != null && rootS.getObject().isResource()) {
                root = rootS.getResource().inModel(model);
            }
            if(matchRoot == null || matchRoot.equals(root)) {
                
                Statement labelS = vio.getProperty(RDFS.label);
                if(labelS != null && labelS.getObject().isLiteral()) {
                    label = labelS.getString();
                }
                else if(label == null) {
                    label = "SPIN constraint at " + SPINLabels.get().getLabel(atClass);
                }
                
                List<SimplePropertyPath> paths = getViolationPaths(model, vio, root);
                List<TemplateCall> fixes = getFixes(cm, model, vio);
                                
                RDFNode value = vio.hasProperty(SPIN.violationValue) ? vio.getRequiredProperty(SPIN.violationValue).getObject() : null;
                Resource level = vio.hasProperty(SPIN.violationLevel) ? vio.getPropertyResourceValue(SPIN.violationLevel) : null;
                                
                results.add(createConstraintViolation(paths, value, fixes, root, label, source, level));
            }
        }
    }
        
    private static ConstraintViolation createConstraintViolation(Collection<SimplePropertyPath> paths,
            RDFNode value,
            Collection<TemplateCall> fixes, 
            Resource instance, 
            String message, 
            Resource source,
            Resource level) {
        ConstraintViolation result = new ConstraintViolation(instance, paths, fixes, message, source);
        result.setValue(value);
        result.setLevel(level);
        return result;
    }
        
    private static List<TemplateCall> getFixes(Model cm, Model model, Resource vio) {
        List<TemplateCall> fixes = new ArrayList<TemplateCall>();
        Iterator<Statement> fit = vio.listProperties(SPIN.fix);
        while(fit.hasNext()) {
            Statement fs = fit.next();
            if(fs.getObject().isResource()) {
                MultiUnion union = JenaUtil.createMultiUnion(new Graph[] {
                        model.getGraph(),
                        cm.getGraph()
                });
                Model unionModel = ModelFactory.createModelForGraph(union);
                Resource r = fs.getResource().inModel(unionModel);
                TemplateCall fix = r.as(TemplateCall.class);
                fixes.add(fix);
            }
        }
        return fixes;
    }
        
    private static List<SimplePropertyPath> getViolationPaths(Model model, Resource vio, Resource root) {
        List<SimplePropertyPath> paths = new ArrayList<>();
        StmtIterator pit = vio.listProperties(SPIN.violationPath);
        while(pit.hasNext()) {
            Statement p = pit.nextStatement();
            if(p.getObject().isURIResource()) {
                Property predicate = model.getProperty(p.getResource().getURI());
                paths.add(new ObjectPropertyPath(root, predicate));
            }
            else if(p.getObject().isAnon()) {
                Resource path = p.getResource();
                if(path.hasProperty(RDF.type, SP.ReversePath)) {
                    Statement reverse = path.getProperty(SP.path);
                    if(reverse != null && reverse.getObject().isURIResource()) {
                        Property predicate = model.getProperty(reverse.getResource().getURI());
                        paths.add(new SubjectPropertyPath(root, predicate));
                    }
                }
            }
        }
        return paths;
    }
        
}
