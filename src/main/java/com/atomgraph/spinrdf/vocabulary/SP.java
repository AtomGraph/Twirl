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
package com.atomgraph.spinrdf.vocabulary;

import com.atomgraph.spinrdf.model.Argument;
import com.atomgraph.spinrdf.model.Command;
import com.atomgraph.spinrdf.model.Query;
import com.atomgraph.spinrdf.model.Template;
import com.atomgraph.spinrdf.model.TemplateCall;
import com.atomgraph.spinrdf.model.impl.ArgumentImpl;
import com.atomgraph.spinrdf.model.impl.CommandImpl;
import com.atomgraph.spinrdf.model.impl.QueryImpl;
import com.atomgraph.spinrdf.model.impl.TemplateCallImpl;
import com.atomgraph.spinrdf.model.impl.TemplateImpl;
import com.atomgraph.spinrdf.model.update.Update;
import com.atomgraph.spinrdf.model.update.impl.UpdateImpl;
import org.apache.jena.enhanced.BuiltinPersonalities;
import org.apache.jena.enhanced.Personality;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Constants for the SP (SPARQL) vocabulary, which provides an RDF representation of SPARQL queries and update
 * requests. Terms are defined by the <a href="http://spinrdf.org/sp">SPIN SP namespace</a>
 * ({@code http://spinrdf.org/sp#}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SP
{

    private SP() { }

    /** Base URI of the namespace: {@value}. */
    public final static String BASE_URI = "http://spinrdf.org/sp";

    /** Namespace URI: {@value}. */
    public final static String NS = BASE_URI + "#";

    /** Recommended namespace prefix: {@value}. */
    public final static String PREFIX = "sp";
    
    /** Namespace URI of SPARQL variables: {@value}. */
    public final static String VAR_NS = "http://spinrdf.org/var#";

    /** Recommended prefix for the variable namespace: {@value}. */
    public final static String VAR_PREFIX = "var";


    /** {@code sp:Aggregation} */
    public final static Resource Aggregation = ResourceFactory.createResource(NS + "Aggregation");

    /** {@code sp:AltPath} */
    public final static Resource AltPath = ResourceFactory.createResource(NS + "AltPath");

    /** {@code sp:Asc} */
    public final static Resource Asc = ResourceFactory.createResource(NS + "Asc");

    /** {@code sp:Ask} */
    public final static Resource Ask = ResourceFactory.createResource(NS + "Ask");

    /** {@code sp:Avg} */
    public final static Resource Avg = ResourceFactory.createResource(NS + "Avg");

    /** {@code sp:Bind} */
    public final static Resource Bind = ResourceFactory.createResource(NS + "Bind");

    /** {@code sp:Clear} */
    public final static Resource Clear = ResourceFactory.createResource(NS + "Clear");

    /** {@code sp:Command} */
    public final static Resource Command = ResourceFactory.createResource(NS + "Command");

    /** {@code sp:Construct} */
    public final static Resource Construct = ResourceFactory.createResource(NS + "Construct");

    /** {@code sp:Count} */
    public final static Resource Count = ResourceFactory.createResource(NS + "Count");

    /** {@code sp:Create} */
    public final static Resource Create = ResourceFactory.createResource(NS + "Create");

    /** {@code sp:DeleteData} */
    public final static Resource DeleteData = ResourceFactory.createResource(NS + "DeleteData");

    /** {@code sp:DeleteWhere} */
    public final static Resource DeleteWhere = ResourceFactory.createResource(NS + "DeleteWhere");

    /** {@code sp:Desc} */
    public final static Resource Desc = ResourceFactory.createResource(NS + "Desc");

    /** {@code sp:Describe} */
    public final static Resource Describe = ResourceFactory.createResource(NS + "Describe");

    /** {@code sp:Drop} */
    public final static Resource Drop = ResourceFactory.createResource(NS + "Drop");

    /** {@code sp:exists} */
    public final static Resource exists = ResourceFactory.createResource(NS + "exists");

    /** {@code sp:Exists} */
    public final static Resource Exists = ResourceFactory.createResource(NS + "Exists");

    /** {@code sp:Expression} */
    public final static Resource Expression = ResourceFactory.createResource(NS + "Expression");

    /** {@code sp:Filter} */
    public final static Resource Filter = ResourceFactory.createResource(NS + "Filter");

    /** {@code sp:InsertData} */
    public final static Resource InsertData = ResourceFactory.createResource(NS + "InsertData");

    /** {@code sp:Load} */
    public final static Resource Load = ResourceFactory.createResource(NS + "Load");

    /** {@code sp:Max} */
    public final static Resource Max = ResourceFactory.createResource(NS + "Max");

    /** {@code sp:Min} */
    public final static Resource Min = ResourceFactory.createResource(NS + "Min");

    /** {@code sp:Modify} */
    public final static Resource Modify = ResourceFactory.createResource(NS + "Modify");

    /** {@code sp:ModPath} */
    public final static Resource ModPath = ResourceFactory.createResource(NS + "ModPath");

    /** {@code sp:Minus} */
    public final static Resource Minus = ResourceFactory.createResource(NS + "Minus");

    /** {@code sp:NamedGraph} */
    public final static Resource NamedGraph = ResourceFactory.createResource(NS + "NamedGraph");

    /** {@code sp:notExists} */
    public final static Resource notExists = ResourceFactory.createResource(NS + "notExists");

    /** {@code sp:NotExists} */
    public final static Resource NotExists = ResourceFactory.createResource(NS + "NotExists");

    /** {@code sp:Optional} */
    public final static Resource Optional = ResourceFactory.createResource(NS + "Optional");

    /** {@code sp:Query} */
    public final static Resource Query = ResourceFactory.createResource(NS + "Query");

    /** {@code sp:ReverseLinkPath} */
    public final static Resource ReverseLinkPath = ResourceFactory.createResource(NS + "ReverseLinkPath");

    /** {@code sp:ReversePath} */
    public final static Resource ReversePath = ResourceFactory.createResource(NS + "ReversePath");

    /** {@code sp:Select} */
    public final static Resource Select = ResourceFactory.createResource(NS + "Select");

    /** {@code sp:Service} */
    public final static Resource Service = ResourceFactory.createResource(NS + "Service");

    /** {@code sp:SeqPath} */
    public final static Resource SeqPath = ResourceFactory.createResource(NS + "SeqPath");

    /** {@code sp:SubQuery} */
    public final static Resource SubQuery = ResourceFactory.createResource(NS + "SubQuery");

    /** {@code sp:Sum} */
    public final static Resource Sum = ResourceFactory.createResource(NS + "Sum");

    /** {@code sp:Triple} */
    public final static Resource Triple = ResourceFactory.createResource(NS + "Triple");

    /** {@code sp:TriplePath} */
    public final static Resource TriplePath = ResourceFactory.createResource(NS + "TriplePath");

    /** {@code sp:TriplePattern} */
    public final static Resource TriplePattern = ResourceFactory.createResource(NS + "TriplePattern");

    /** {@code sp:TripleTemplate} */
    public final static Resource TripleTemplate = ResourceFactory.createResource(NS + "TripleTemplate");

    /** {@code sp:undef} */
    public final static Resource undef = ResourceFactory.createResource(NS + "undef");

    /** {@code sp:Union} */
    public final static Resource Union = ResourceFactory.createResource(NS + "Union");

    /** {@code sp:Update} */
    public final static Resource Update = ResourceFactory.createResource(NS + "Update");

    /** {@code sp:Values} */
    public final static Resource Values = ResourceFactory.createResource(NS + "Values");

    /** {@code sp:Variable} */
    public final static Resource Variable = ResourceFactory.createResource(NS + "Variable");


    /** {@code sp:all} */
    public final static Property all = ResourceFactory.createProperty(NS + "all");

    /** {@code sp:arg} */
    public final static Property arg = ResourceFactory.createProperty(NS + "arg");

    /** {@code sp:arg1} */
    public final static Property arg1 = ResourceFactory.createProperty(NS + "arg1");

    /** {@code sp:arg2} */
    public final static Property arg2 = ResourceFactory.createProperty(NS + "arg2");

    /** {@code sp:arg3} */
    public final static Property arg3 = ResourceFactory.createProperty(NS + "arg3");

    /** {@code sp:arg4} */
    public final static Property arg4 = ResourceFactory.createProperty(NS + "arg4");

    /** {@code sp:arg5} */
    public final static Property arg5 = ResourceFactory.createProperty(NS + "arg5");
    
    /** {@code sp:as} */
    public final static Property as = ResourceFactory.createProperty(NS + "as");
    
    /** {@code sp:bindings} */
    public final static Property bindings = ResourceFactory.createProperty(NS + "bindings");

    /** {@code sp:data} */
    public final static Property data = ResourceFactory.createProperty(NS + "data");

    /** {@code sp:default} */
    public final static Property default_ = ResourceFactory.createProperty(NS + "default");
    
    /** {@code sp:deletePattern} */
    public final static Property deletePattern = ResourceFactory.createProperty(NS + "deletePattern");
    
    /** {@code sp:distinct} */
    public final static Property distinct = ResourceFactory.createProperty(NS + "distinct");
    
    /** {@code sp:document} */
    public final static Property document = ResourceFactory.createProperty(NS + "document");
    
    /** {@code sp:elements} */
    public final static Property elements = ResourceFactory.createProperty(NS + "elements");
    
    /** {@code sp:expression} */
    public final static Property expression = ResourceFactory.createProperty(NS + "expression");
    
    /** {@code sp:from} */
    public final static Property from = ResourceFactory.createProperty(NS + "from");
    
    /** {@code sp:fromNamed} */
    public final static Property fromNamed = ResourceFactory.createProperty(NS + "fromNamed");

    /** {@code sp:graphIRI} */
    public final static Property graphIRI = ResourceFactory.createProperty(NS + "graphIRI");
    
    /** {@code sp:graphNameNode} */
    public final static Property graphNameNode = ResourceFactory.createProperty(NS + "graphNameNode");
    
    /** {@code sp:groupBy} */
    public final static Property groupBy = ResourceFactory.createProperty(NS + "groupBy");
    
    /** {@code sp:having} */
    public final static Property having = ResourceFactory.createProperty(NS + "having");
    
    /** {@code sp:insertPattern} */
    public final static Property insertPattern = ResourceFactory.createProperty(NS + "insertPattern");
    
    /** {@code sp:into} */
    public final static Property into = ResourceFactory.createProperty(NS + "into");
    
    /** {@code sp:limit} */
    public final static Property limit = ResourceFactory.createProperty(NS + "limit");
    
    /** {@code sp:modMax} */
    public final static Property modMax = ResourceFactory.createProperty(NS + "modMax");
    
    /** {@code sp:modMin} */
    public final static Property modMin = ResourceFactory.createProperty(NS + "modMin");

    /** {@code sp:named} */
    public final static Property named = ResourceFactory.createProperty(NS + "named");
    
    /** {@code sp:node} */
    public final static Property node = ResourceFactory.createProperty(NS + "node");
    
    /** {@code sp:object} */
    public final static Property object = ResourceFactory.createProperty(NS + "object");
    
    /** {@code sp:offset} */
    public final static Property offset = ResourceFactory.createProperty(NS + "offset");
    
    /** {@code sp:orderBy} */
    public final static Property orderBy = ResourceFactory.createProperty(NS + "orderBy");
    
    /** {@code sp:path} */
    public final static Property path = ResourceFactory.createProperty(NS + "path");
    
    /** {@code sp:path1} */
    public final static Property path1 = ResourceFactory.createProperty(NS + "path1");
    
    /** {@code sp:path2} */
    public final static Property path2 = ResourceFactory.createProperty(NS + "path2");

    /** {@code sp:predicate} */
    public final static Property predicate = ResourceFactory.createProperty(NS + "predicate");
    
    /** {@code sp:query} */
    public final static Property query = ResourceFactory.createProperty(NS + "query");

    /** {@code sp:reduced} */
    public final static Property reduced = ResourceFactory.createProperty(NS + "reduced");

    /** {@code sp:resultNodes} */
    public final static Property resultNodes = ResourceFactory.createProperty(NS + "resultNodes");

    /** {@code sp:resultVariables} */
    public final static Property resultVariables = ResourceFactory.createProperty(NS + "resultVariables");
    
    /** {@code sp:separator} */
    public final static Property separator = ResourceFactory.createProperty(NS + "separator");
    
    /** {@code sp:serviceURI} */
    public final static Property serviceURI = ResourceFactory.createProperty(NS + "serviceURI");
    
    /** {@code sp:silent} */
    public final static Property silent = ResourceFactory.createProperty(NS + "silent");

    /** {@code sp:str} */
    public final static Property str = ResourceFactory.createProperty(NS + "str");

    /** {@code sp:strlang} */
    public final static Property strlang = ResourceFactory.createProperty(NS + "strlang");

    /** {@code sp:subject} */
    public final static Property subject = ResourceFactory.createProperty(NS + "subject");

    /** {@code sp:subPath} */
    public final static Property subPath = ResourceFactory.createProperty(NS + "subPath");

    /** {@code sp:templates} */
    public final static Property templates = ResourceFactory.createProperty(NS + "templates");

    /** {@code sp:text} */
    public final static Property text = ResourceFactory.createProperty(NS + "text");
    
    /** {@code sp:using} */
    public final static Property using = ResourceFactory.createProperty(NS + "using");
    
    /** {@code sp:usingNamed} */
    public final static Property usingNamed = ResourceFactory.createProperty(NS + "usingNamed");
    
    /** {@code sp:values} */
    public final static Property values = ResourceFactory.createProperty(NS + "values");

    /** {@code sp:variable} */
    public final static Property variable = ResourceFactory.createProperty(NS + "variable");
    
    /** {@code sp:varName} */
    public final static Property varName = ResourceFactory.createProperty(NS + "varName");
    
    /** {@code sp:varNames} */
    public final static Property varNames = ResourceFactory.createProperty(NS + "varNames");
    
    /** {@code sp:where} */
    public final static Property where = ResourceFactory.createProperty(NS + "where");
    
    /** {@code sp:with} */
    public final static Property with = ResourceFactory.createProperty(NS + "with");
    
    
    /** {@code sp:bound} */
    public final static Resource bound = ResourceFactory.createResource(NS + "bound");
    
    /** {@code sp:eq} */
    public final static Resource eq = ResourceFactory.createResource(NS + "eq");
    
    /** {@code sp:not} */
    public final static Resource not = ResourceFactory.createResource(NS + "not");

    /** {@code sp:regex} */
    public final static Resource regex = ResourceFactory.createResource(NS + "regex");

    /** {@code sp:sub} */
    public final static Resource sub = ResourceFactory.createResource(NS + "sub");

    /** {@code sp:unaryMinus} */
    public final static Resource unaryMinus = ResourceFactory.createResource(NS + "unaryMinus");

    /**
     * Returns the 1-based argument index encoded in a SPIN argument variable name such as {@code arg1}.
     * @param varName  the variable name, expected to start with {@code arg}
     * @return the argument index, or {@code null} if the name is not an argument variable
     */
    public static Integer getArgPropertyIndex(String varName)
    {
        if (varName.startsWith("arg"))
        {
            String subString = varName.substring(3);
            try
            {
                return Integer.getInteger(subString);
            }
            catch(Throwable t)
            {
            }
        }
        
        return null;
    }
    
    /**
     * SPIN-aware enhanced-node personality: a private copy of the standard personality with the SPIN model
     * implementations ({@link Query}/{@link TemplateCall}/{@link Command}/...) registered. Self-contained — it does
     * NOT mutate the global {@link BuiltinPersonalities#model}, so SPIN polymorphism composes with any model
     * (plain, legacy, or ontapi) once a graph is wrapped in a Model that uses this personality, with no reliance on
     * global registration or class-init order.
     */
    public static final Personality<RDFNode> personality = init(BuiltinPersonalities.model.copy());

    /**
     * Registers the SPIN model implementations ({@link Template}, {@link Argument}, {@link TemplateCall},
     * {@link Command}, {@link Query} and {@link Update}) on the given enhanced-node personality.
     * @param p  the personality to augment
     * @return the same personality, for chaining
     */
    public static Personality<RDFNode> init(Personality<RDFNode> p)
    {
        p.add(Template.class, TemplateImpl.factory);
        p.add(Argument.class, ArgumentImpl.factory);
        p.add(TemplateCall.class, TemplateCallImpl.factory);
        p.add(Command.class, CommandImpl.factory);
        p.add(Query.class, QueryImpl.factory);
        p.add(Update.class, UpdateImpl.factory);
        return p;
    }
    
}
