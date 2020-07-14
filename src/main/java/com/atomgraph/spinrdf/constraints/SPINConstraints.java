/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */
package com.atomgraph.spinrdf.constraints;

import com.atomgraph.spinrdf.model.TemplateCall;
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
import com.atomgraph.spinrdf.vocabulary.SP;
import com.atomgraph.spinrdf.vocabulary.SPIN;
import java.util.HashSet;
import java.util.Set;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.shared.PropertyNotFoundException;
import org.apache.jena.util.iterator.ExtendedIterator;

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
    
    /**
     * Checks all instances in a given Model against all spin:constraints and
     * returns a List of constraint violations. 
     * A ProgressMonitor can be provided to enable the user to get intermediate
     * status reports and to cancel the operation.
     * @param model  the Model to operate on
     * @return a List of ConstraintViolations
     */
    public static List<ConstraintViolation> check(OntModel model)
    {
        return check(model, SPIN.constraint);
    }

    /**
     * Checks all instances in a given Model and returns a List of constraint violations. 
     * @param model  the Model to operate on
     * @param predicate  the system property, e.g. a sub-property of spin:constraint
     * @return a List of ConstraintViolations
     */
    public static List<ConstraintViolation> check(OntModel model, Property predicate)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        Map<Resource, List<QueryWrapper>> class2Query = class2Query(model, predicate);
        for (Resource cls : class2Query.keySet())
        {
            List<QueryWrapper> wrappers = class2Query.get(cls);
            for (QueryWrapper wrapper : wrappers)
            {
                cvs.addAll(runQueryOnClass(wrapper, cls, model));
            
                // run the same constraint query on subclasses
                Set<Resource> subClasses = getSubClasses(cls);
                for (Resource subCls : subClasses)
                    cvs.addAll(runQueryOnClass(wrapper, subCls, model));
            }
        }
        
        return cvs;
    }
    
    protected static Set<Resource> getSubClasses(Resource cls)
    {
        Set<Resource> subClasses = new HashSet<>();
        
        StmtIterator it = cls.getModel().listStatements(null, RDFS.subClassOf, cls);
        try
        {
            while (it.hasNext())
            {
                Statement stmt = it.next();
                if (stmt.getSubject().isResource())
                {
                    Resource subCls = stmt.getSubject().asResource();
                    if (!subCls.equals(cls))
                    {
                        subClasses.add(subCls);
                        subClasses.addAll(getSubClasses(subCls));
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return subClasses;
    }
    
    protected static Map<Resource, List<QueryWrapper>> class2Query(OntModel model, Property predicate)
    {
        Map<Resource, List<QueryWrapper>> class2Query = new HashMap<>();
                
        StmtIterator constraintIt = model.listStatements((Resource)null, predicate, (Resource)null);
        try
        {
            while (constraintIt.hasNext())
            {
                Statement stmt = constraintIt.next();
                addClassContraints(stmt, predicate, class2Query);
            }
        }
        finally
        {
            constraintIt.close();
        }
        
        return class2Query;
    }
    
    protected static void addClassContraints(Statement stmt, Property predicate, Map<Resource, List<QueryWrapper>> class2Query)
    {
        Resource cls = stmt.getSubject();
        Resource constraint = stmt.getResource();

        QueryWrapper wrapper = createWrapper(constraint);
        if (wrapper == null) return;

        if (class2Query.containsKey(cls))
            class2Query.get(cls).add(wrapper);
        else
        {
            List<QueryWrapper> wrapperList = new ArrayList<>();
            wrapperList.add(wrapper);
            class2Query.put(cls, wrapperList);
        }

        if (cls.canAs(OntClass.class))
        {
            OntClass ontCls = cls.as(OntClass.class);
            ExtendedIterator<OntClass> classIt = ontCls.listSuperClasses();
            try
            {
                while (classIt.hasNext())
                {
                    OntClass superCls = classIt.next();
                    StmtIterator constraintIt = superCls.listProperties(predicate);
                    try
                    {
                        while (constraintIt.hasNext())
                        {
                            addClassContraints(constraintIt.next(), predicate, class2Query);
                        }
                    }
                    finally
                    {
                        constraintIt.close();
                    }
                }
            }
            finally
            {
                classIt.close();
            }
        }
    }
    
    protected static QueryWrapper createWrapper(Resource constraint)
    {
        final Query constraintQuery;

        if (constraint.canAs(com.atomgraph.spinrdf.model.Query.class))
        {
            try
            {
                com.atomgraph.spinrdf.model.Query query = constraint.as(com.atomgraph.spinrdf.model.Query.class);
                constraintQuery = QueryFactory.create(query.getText());
            }
            catch (PropertyNotFoundException ex)
            {
                return null;
            }
        }
        else
            return null;

        final QuerySolutionMap qsm;
        if (constraint.canAs(TemplateCall.class)) qsm = constraint.as(TemplateCall.class).getInitialBinding();
        else qsm = new QuerySolutionMap();

        return new QueryWrapper(constraint, constraintQuery, qsm);
    }
    
    protected static List<ConstraintViolation> runQueryOnClass(QueryWrapper wrapper, Resource cls, Model model)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();
        
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.addAll(wrapper.getQuerySolutionMap());

        ResIterator it = model.listSubjectsWithProperty(RDF.type, cls);
        try
        {
            while (it.hasNext())
            {
                Resource instance = it.next();

                qsm.add(SPIN.THIS_VAR_NAME, instance);

                try (QueryExecution qex = QueryExecutionFactory.create(wrapper.getQuery(), model, qsm))
                {
                    //ResultSetFormatter.out(System.out, qex.execSelect());

                    cvs.addAll(convertToConstraintViolations(qex.execConstruct(), model, cls, null, null, wrapper.getSource()));
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return cvs;
    }

    private static List<ConstraintViolation> convertToConstraintViolations(
            Model cm,
            Model model,
            Resource atClass,
            Resource matchRoot,
            String label,
            Resource source)
    {
        List<ConstraintViolation> results = new ArrayList<>();

        StmtIterator it = cm.listStatements(null, RDF.type, SPIN.ConstraintViolation);
        while(it.hasNext()) {
            Statement s = it.nextStatement();
            Resource vio = s.getSubject();
            
            Resource root = null;
            Statement rootS = vio.getProperty(SPIN.violationRoot);
            if (rootS != null && rootS.getObject().isResource()) {
                root = rootS.getResource().inModel(model);
            }
            if (matchRoot == null || matchRoot.equals(root)) {
                
                Statement labelS = vio.getProperty(RDFS.label);
                if (labelS != null && labelS.getObject().isLiteral()) {
                    label = labelS.getString();
                }
                else if (label == null) {
                    label = "SPIN constraint at " + getLabel(atClass);
                }
                
                List<SimplePropertyPath> paths = getViolationPaths(model, vio, root);
                List<TemplateCall> fixes = getFixes(cm, model, vio);
                                
                RDFNode value = vio.hasProperty(SPIN.violationValue) ? vio.getRequiredProperty(SPIN.violationValue).getObject() : null;
                Resource level = vio.hasProperty(SPIN.violationLevel) ? vio.getPropertyResourceValue(SPIN.violationLevel) : null;
                                
                results.add(createConstraintViolation(paths, value, fixes, root, label, source, level));
            }
        }
        
        return results;
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
        List<TemplateCall> fixes = new ArrayList<>();
        Iterator<Statement> fit = vio.listProperties(SPIN.fix);
        while (fit.hasNext())
        {
            Statement fs = fit.next();
            if(fs.getObject().isResource()) {
                MultiUnion union = new MultiUnion(new Graph[] {
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
        while (pit.hasNext())
        {
            Statement p = pit.nextStatement();
            if(p.getObject().isURIResource())
            {
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
        
    /**
     * Creates an RDF representation (instances of spin:ConstraintViolation) from a
     * collection of ConstraintViolation Java objects. 
     * @param cvs  the violation objects
     * @param result  the Model to add the results to
     * @param createSource  true to also create the spin:violationSource
     */
    public static void addConstraintViolationsRDF(List<ConstraintViolation> cvs, Model result, boolean createSource)
    {
        for(ConstraintViolation cv : cvs)
        {
            Resource r = result.createResource(SPIN.ConstraintViolation);
            String message = cv.getMessage();
            if (message != null && message.length() > 0) r.addProperty(RDFS.label, message);
            if (cv.getRoot() != null) r.addProperty(SPIN.violationRoot, cv.getRoot());
            r.addProperty(SPIN.violationLevel, cv.getLevel());
            
            for(SimplePropertyPath path : cv.getPaths())
            {
                if(path instanceof ObjectPropertyPath)
                {
                    r.addProperty(SPIN.violationPath, path.getPredicate());
                }
                else
                {
                    Resource p = result.createResource(SP.ReversePath);
                    p.addProperty(SP.path, path.getPredicate());
                    r.addProperty(SPIN.violationPath, p);
                }
            }
            
            if (createSource && cv.getSource() != null) r.addProperty(SPIN.violationSource, cv.getSource());
            if (cv.getValue() != null) r.addProperty(SPIN.violationValue, cv.getValue());
        }
    }
        
    /**
     * Gets the label for a given Resource.
     * @param resource  the Resource to get the label of
     * @return the label (never null)
     */
    public static String getLabel(Resource resource)
    {
        if (resource.isURIResource() && resource.getModel() != null)
        {
            String qname = resource.getModel().qnameFor(resource.getURI());
            if(qname != null) return qname;
            else return "<" + resource.getURI() + ">";
        }
        else return resource.toString();
    }
        
}
