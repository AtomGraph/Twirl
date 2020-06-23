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
package com.atomgraph.spinrdf.model.impl;

import com.atomgraph.spinrdf.model.Template;
import com.atomgraph.spinrdf.model.TemplateCall;
import com.atomgraph.spinrdf.vocabulary.SP;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.enhanced.EnhNode;
import org.apache.jena.enhanced.Implementation;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.ConversionException;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class CommandImpl extends ResourceImpl implements TemplateCall
{
    
    public static Implementation factory = new Implementation() 
    {
        
        @Override
        public EnhNode wrap(Node node, EnhGraph enhGraph)
        {
            if (canWrap(node, enhGraph))
            {
                return new CommandImpl(node, enhGraph);
            }
            else
            {
                throw new ConversionException( "Cannot convert node " + node.toString() + " to Command: it does not have rdf:type sp:Command or equivalent");
            }
        }

        @Override
        public boolean canWrap(Node node, EnhGraph eg)
        {
            if (eg == null) throw new IllegalArgumentException("EnhGraph cannot be null");

            return eg.asGraph().contains(node, RDF.type.asNode(), SP.Command.asNode());
        }
        
    };
    
    public CommandImpl(Node node, EnhGraph graph)
    {
        super(node, graph);
    }
    
    @Override
    public String getText()
    {
        if (hasProperty(SP.text))
            return getProperty(SP.text).getString();
        else
            return getTemplate().getBody().getText();
    }
    
    @Override
    public Template getTemplate()
    {
        StmtIterator it = listProperties(RDF.type);
        try
        {
            while (it.hasNext())
            {
                Statement s = it.next();

                if (s.getObject().canAs(Template.class)) return s.getObject().as(Template.class);
            }
        }
        finally
        {
            it.close();
        }
        
        return null;
    }

//    @Override
//    public Map<Argument, RDFNode> getArgumentsMap() {
//        Map<Argument, RDFNode> map = new HashMap<>();
//        Template template = getTemplate();
//
//        template.getArguments(false).forEach((ad) ->
//        {
//            Property argProperty = ad.getPredicate();
//            if (argProperty != null) {
//                Statement valueS = getProperty(argProperty);
//                if (valueS != null) {
//                    map.put(ad, valueS.getObject());
//                }
//            }
//        });
//        
//        return map;
//    }

    @Override
    public QuerySolutionMap getInitialBinding()
    {
        QuerySolutionMap map = new QuerySolutionMap();
        Map<String,RDFNode> input = getArgumentsMapByVarNames();
        input.keySet().forEach((varName) ->
        {
            RDFNode value = input.get(varName);
            map.add(varName, value);
        });
        return map;
    }
    
    public Map<String, RDFNode> getArgumentsMapByVarNames()
    {
        Map<String,RDFNode> map = new HashMap<>();
        Template template = getTemplate();
        
        template.getArguments(false).forEach((ad) ->
        {
            Property argProperty = ad.getPredicate();
            if (argProperty != null) {
                String varName = ad.getVarName();
                Statement valueS = getProperty(argProperty);
                if(valueS != null) {
                    map.put(varName, valueS.getObject());
                }
                else if(ad.getDefaultValue() != null) {
                    map.put(varName, ad.getDefaultValue());
                }
            }
        });
        
        return map;
    }
    
}
