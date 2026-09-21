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
import org.apache.jena.graph.Node;
import org.apache.jena.graph.compose.MultiUnion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.modify.request.QuadAcc;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.PatternVars;
import org.apache.jena.sparql.syntax.Template;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.ModelCom;
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
import org.apache.jena.shared.PropertyNotFoundException;

/**
 * Entry point for validating RDF data against SPIN constraints. It inspects a model for {@code spin:constraint}
 * definitions attached to classes, executes each constraint's CONSTRUCT query against the instances of those
 * classes (and their subclasses) and collects the resulting {@link ConstraintViolation}s. Only CONSTRUCT-based
 * constraints are supported; ASK constraints are not.
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SPINConstraints
{

    private SPINConstraints() { }

    /**
     * Pairs a parsed constraint query with the resource it originates from and the variable bindings (template
     * arguments) to apply when executing it.
     */
    public static class QueryWrapper
    {

        private final Resource source;
        private final Query query;
        private final QuerySolutionMap qsm;

        /**
         * Constructs a query wrapper.
         * @param source  the SPIN query or template call the constraint originates from
         * @param query  the parsed SPARQL query
         * @param qsm  the variable bindings (template arguments) to apply
         */
        public QueryWrapper(Resource source, Query query, QuerySolutionMap qsm)
        {
            this.source = source;
            this.query = query;
            this.qsm = qsm;
        }

        /**
         * Returns the SPIN query or template call this constraint originates from.
         * @return the source resource
         */
        public Resource getSource()
        {
            return source;
        }

        /**
         * Returns the parsed SPARQL query.
         * @return the query
         */
        public Query getQuery()
        {
            return query;
        }

        /**
         * Returns the variable bindings (template arguments) to apply when executing the query.
         * @return the query solution map
         */
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
    public static List<ConstraintViolation> check(Model model)
    {
        return check(model, SPIN.constraint, model);
    }

    /**
     * Checks all instances in a given Model against the {@code spin:constraint}s found in a separate constraint
     * Model and returns a List of constraint violations.
     * @param model  the Model whose instances are validated
     * @param constraintModel  the Model that holds the constraint definitions
     * @return a List of ConstraintViolations
     */
    public static List<ConstraintViolation> check(Model model, Model constraintModel)
    {
        return check(model, SPIN.constraint, constraintModel);
    }
    
    /**
     * Checks all instances in a given Model and returns a List of constraint violations. 
     * @param model  the Model to operate on
     * @param predicate  the system property, e.g. a sub-property of spin:constraint
     * @return a List of ConstraintViolations
     */
    public static List<ConstraintViolation> check(Model model, Property predicate)
    {
        return check(model, predicate, model);
    }
    
    /**
     * Checks all instances in a given Model against constraints declared with a given predicate in a separate
     * constraint Model and returns a List of constraint violations.
     * @param model  the Model whose instances are validated
     * @param predicate  the system property, e.g. a sub-property of {@code spin:constraint}
     * @param constraintModel  the Model that holds the constraint definitions
     * @return a List of ConstraintViolations
     */
    public static List<ConstraintViolation> check(Model model, Property predicate, Model constraintModel)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();

        // Re-base the constraint model onto the SPIN-aware personality so that constraint resources resolve via
        // canAs(Query)/canAs(TemplateCall) regardless of the caller's model type — plain Model, legacy OntModel, or
        // ontapi OntModel (whose own profile-aware personality does not carry the SPIN implementations).
        constraintModel = new ModelCom(constraintModel.getGraph(), SP.personality);

        Map<Resource, List<QueryWrapper>> class2Query = class2Query(constraintModel, predicate);
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
    
    /**
     * Collects all direct and transitive {@code rdfs:subClassOf} subclasses of a class.
     * @param cls  the class whose subclasses are collected
     * @return the set of subclasses (excluding the class itself)
     */
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

    /**
     * Collects the {@code rdfs:subClassOf} superclasses of a class.
     * @param cls  the class whose superclasses are collected
     * @return the set of superclasses (excluding the class itself)
     */
    protected static Set<Resource> getSuperClasses(Resource cls)
    {
        Set<Resource> superClasses = new HashSet<>();
        
        StmtIterator it = cls.getModel().listStatements(cls, RDFS.subClassOf, (RDFNode)null);
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
                        superClasses.add(subCls);
                        superClasses.addAll(getSubClasses(subCls));
                    }
                }
            }
        }
        finally
        {
            it.close();
        }
        
        return superClasses;
    }

    /**
     * Builds a map from each class to the constraint queries attached to it via the given predicate.
     * @param model  the Model to scan for constraint definitions
     * @param predicate  the constraint predicate, e.g. {@code spin:constraint}
     * @return a map from class to its constraint query wrappers
     */
    protected static Map<Resource, List<QueryWrapper>> class2Query(Model model, Property predicate)
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
    
    /**
     * Adds the constraint carried by a statement to the class-to-query map, and recursively adds the constraints
     * inherited from the class's superclasses.
     * @param stmt  the statement linking a class to a constraint via the predicate
     * @param predicate  the constraint predicate, e.g. {@code spin:constraint}
     * @param class2Query  the map to populate
     */
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

        Set<Resource> superClasses = getSuperClasses(cls);
        for (Resource superClass : superClasses)
        {
            StmtIterator constraintIt = superClass.listProperties(predicate);
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
    
    /**
     * Creates a query wrapper for a constraint resource, parsing its SPARQL text and collecting any template
     * argument bindings.
     * @param constraint  the constraint resource (a SPIN query or template call)
     * @return the query wrapper, or {@code null} if the constraint is not a usable query
     */
    protected static QueryWrapper createWrapper(Resource constraint)
    {
        final Query constraintQuery;

        if (constraint.canAs(com.atomgraph.spinrdf.model.Query.class))
        {
            try
            {
                com.atomgraph.spinrdf.model.Query query = constraint.as(com.atomgraph.spinrdf.model.Query.class);
                constraintQuery = bindThisInTemplate(QueryFactory.create(query.getText()));
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
    
    /**
     * Routes {@code ?this} into the CONSTRUCT template through the WHERE clause. {@code QueryExecution.substitution()}
     * rewrites the parsed query syntactically, so a blank-node instance substituted for {@code ?this} becomes a
     * blank node written in the template, and template instantiation mints a fresh one per solution (SPARQL 1.1
     * Query, "Templates with Blank Nodes"): the violation root then denotes nothing in the checked model. Named
     * instances are unaffected, because an IRI written in a template is a constant. Renaming the template's
     * {@code ?this} to a fresh variable and binding that variable from {@code ?this} at the end of the WHERE clause
     * keeps the substituted term out of the template: the instance reaches it as a variable value, which
     * instantiation copies through unchanged, as the pre-Jena 6 initial binding did. The WHERE clause still sees the
     * substituted constant everywhere, filters included, and the appended BIND adds no rows. A query that groups
     * has the fresh variable added to its GROUP BY, or grouping would hide it from the template.
     * The SPARQL text of the constraint is not touched; only the parsed query is.
     * @param query  the parsed constraint query, modified in place
     * @return the same query
     */
    protected static Query bindThisInTemplate(Query query)
    {
        if (!query.isConstructType()) return query;

        Var thisVar = Var.alloc(SPIN.THIS_VAR_NAME);
        Template template = query.getConstructTemplate();

        Set<Var> templateVars = new HashSet<>();
        for (Quad quad : template.getQuads())
            for (Node node : List.of(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject()))
                if (Var.isVar(node)) templateVars.add(Var.alloc(node));
        if (!templateVars.contains(thisVar)) return query;

        // a variable the query does not use anywhere - pattern, sub-selects, template, GROUP BY
        Set<Var> usedVars = new HashSet<>(templateVars);
        usedVars.addAll(PatternVars.vars(query.getQueryPattern()));
        usedVars.addAll(query.getGroupBy().getVars());
        Var freshVar = Var.alloc(SPIN.THIS_VAR_NAME + "_");
        while (usedVars.contains(freshVar)) freshVar = Var.alloc(freshVar.getVarName() + "_");
        final Var boundVar = freshVar;

        NodeTransform rename = node -> thisVar.equals(node) ? boundVar : node;
        if (template.containsRealQuad()) query.setConstructTemplate(new Template(new QuadAcc(NodeTransformLib.transformQuads(rename, template.getQuads()))));
        else query.setConstructTemplate(new Template(NodeTransformLib.transform(rename, template.getBGP())));

        Element pattern = query.getQueryPattern();
        final ElementGroup group;
        if (pattern instanceof ElementGroup elementGroup) group = elementGroup;
        else
        {
            group = new ElementGroup();
            if (pattern != null) group.addElement(pattern);
        }
        group.addElement(new ElementBind(boundVar, new ExprVar(thisVar)));
        query.setQueryPattern(group);

        if (query.hasGroupBy()) query.addGroupBy(boundVar);

        return query;
    }

    /**
     * Executes a constraint query against every instance of a class and collects the resulting violations. The
     * query is run once per instance, with {@code ?this} bound to the instance.
     * @param wrapper  the constraint query to execute
     * @param cls  the class whose instances are checked
     * @param model  the Model to query for instances
     * @return the constraint violations produced for the class's instances
     */
    protected static List<ConstraintViolation> runQueryOnClass(QueryWrapper wrapper, Resource cls, Model model)
    {
        List<ConstraintViolation> cvs = new ArrayList<>();

        QuerySolutionMap qsm = wrapper.getQuerySolutionMap();

        ResIterator it = model.listSubjectsWithProperty(RDF.type, cls);
        try
        {
            while (it.hasNext())
            {
                Resource instance = it.next();

                BindingBuilder bb = BindingFactory.builder();
                // Add template arg bindings from QSM
                Iterator<String> varNames = qsm.varNames();
                while (varNames.hasNext())
                {
                    String varName = varNames.next();
                    bb.add(Var.alloc(varName), qsm.get(varName).asNode());
                }
                bb.add(Var.alloc(SPIN.THIS_VAR_NAME), instance.asNode());
                Binding binding = bb.build();

                // QueryExecution.initialBinding() was replaced by .substitution() in Jena 6 — see https://github.com/apache/jena/issues/3267
                try (QueryExecution qex = QueryExecution.create().query(wrapper.getQuery()).model(model).substitution(binding).build())
                {
                    //ResultSetFormatter.out(System.out, qex.execSelect());

                    cvs.addAll(convertToConstraintViolations(qex.execConstruct(), model, null, null, wrapper.getSource()));
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

                // per-violation message: the CONSTRUCT-emitted rdfs:label wins, then the caller-supplied
                // label, then the constraint resource's own rdfs:label. No authored label means no message -
                // a violation must not grow boilerplate text that consumers could mistake for an authored
                // constraint message, and one violation's label must not leak into the next (the label
                // parameter used to double as the loop accumulator)
                String message = label;
                Statement labelS = vio.getProperty(RDFS.label);
                if (labelS != null && labelS.getObject().isLiteral()) {
                    message = labelS.getString();
                }
                else if (message == null && source != null) {
                    Statement sourceLabelS = source.getProperty(RDFS.label);
                    if (sourceLabelS != null && sourceLabelS.getObject().isLiteral()) message = sourceLabelS.getString();
                }

                List<SimplePropertyPath> paths = getViolationPaths(model, vio, root);
                List<TemplateCall> fixes = getFixes(cm, model, vio);

                RDFNode value = vio.hasProperty(SPIN.violationValue) ? vio.getRequiredProperty(SPIN.violationValue).getObject() : null;
                Resource level = vio.hasProperty(SPIN.violationLevel) ? vio.getPropertyResourceValue(SPIN.violationLevel) : null;

                results.add(createConstraintViolation(paths, value, fixes, root, message, source, level));
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
