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

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

/**
 * Constants for the SPIN vocabulary, which attaches SPARQL queries and update requests to RDFS/OWL classes as
 * constraints, rules and constructors. Terms are defined by the
 * <a href="http://spinrdf.org/spin">SPIN namespace</a> ({@code http://spinrdf.org/spin#}).
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class SPIN
{

    private SPIN() { }


    /** Base URI of the namespace: {@value}. */
    public final static String BASE_URI = "http://spinrdf.org/spin";
    
    /** Namespace URI: {@value}. */
    public final static String NS = BASE_URI + "#";
    
    /** Recommended namespace prefix: {@value}. */
    public final static String PREFIX = "spin";
    
    
    /** Name of the variable bound to the focus node ({@code ?this}): {@value}. */
    public final static String THIS_VAR_NAME = "this";


    /** {@code spin:ask} */
    public final static Resource ask = ResourceFactory.createResource(NS + "ask");

    /** {@code spin:AskTemplate} */
    public final static Resource AskTemplate = ResourceFactory.createProperty(NS + "AskTemplate");

    /** {@code spin:Column} */
    public final static Resource Column = ResourceFactory.createProperty(NS + "Column");

    /** {@code spin:ConstraintViolation} */
    public final static Resource ConstraintViolation = ResourceFactory.createProperty(NS + "ConstraintViolation");

    /** {@code spin:ConstraintViolationLevel} */
    public final static Resource ConstraintViolationLevel = ResourceFactory.createProperty(NS + "ConstraintViolationLevel");

    /** {@code spin:construct} */
    public final static Resource construct = ResourceFactory.createResource(NS + "construct");

    /** {@code spin:constructViolations} */
    public final static Resource constructViolations = ResourceFactory.createResource(NS + "constructViolations");

    /** {@code spin:ConstructTemplate} */
    public final static Resource ConstructTemplate = ResourceFactory.createProperty(NS + "ConstructTemplate");

    /** {@code spin:Error} */
    public final static Resource Error = ResourceFactory.createProperty(NS + "Error");

    /** {@code spin:eval} */
    public final static Resource eval = ResourceFactory.createResource(NS + "eval");

    /** {@code spin:Fatal} */
    public final static Resource Fatal = ResourceFactory.createProperty(NS + "Fatal");

    /** {@code spin:Function} */
    public final static Resource Function = ResourceFactory.createResource(NS + "Function");

    /** {@code spin:Functions} */
    public final static Resource Functions = ResourceFactory.createResource(NS + "Functions");

    /** {@code spin:Info} */
    public final static Resource Info = ResourceFactory.createProperty(NS + "Info");

    /** {@code spin:LibraryOntology} */
    public final static Resource LibraryOntology = ResourceFactory.createResource(NS + "LibraryOntology");

    /** {@code spin:MagicProperties} */
    public final static Resource MagicProperties = ResourceFactory.createResource(NS + "MagicProperties");

    /** {@code spin:MagicProperty} */
    public final static Resource MagicProperty = ResourceFactory.createResource(NS + "MagicProperty");

    /** {@code spin:Module} */
    public final static Resource Module = ResourceFactory.createResource(NS + "Module");

    /** {@code spin:Modules} */
    public final static Resource Modules = ResourceFactory.createResource(NS + "Modules");

    /** {@code spin:Rule} */
    public final static Resource Rule = ResourceFactory.createProperty(NS + "Rule");

    /** {@code spin:RuleProperty} */
    public final static Resource RuleProperty = ResourceFactory.createProperty(NS + "RuleProperty");

    /** {@code spin:select} */
    public final static Resource select = ResourceFactory.createResource(NS + "select");

    /** {@code spin:SelectTemplate} */
    public final static Resource SelectTemplate = ResourceFactory.createProperty(NS + "SelectTemplate");

    /** {@code spin:TableDataProvider} */
    public final static Resource TableDataProvider = ResourceFactory.createProperty(NS + "TableDataProvider");

    /** {@code spin:Template} */
    public final static Resource Template = ResourceFactory.createProperty(NS + "Template");

    /** {@code spin:Templates} */
    public final static Resource Templates = ResourceFactory.createProperty(NS + "Templates");

    /** {@code spin:UpdateTemplate} */
    public final static Resource UpdateTemplate = ResourceFactory.createProperty(NS + "UpdateTemplate");

    /** {@code spin:violatesConstraints} */
    public final static Resource violatesConstraints = ResourceFactory.createResource(NS + "violatesConstraints");

    /** {@code spin:Warning} */
    public final static Resource Warning = ResourceFactory.createProperty(NS + "Warning");

    
    /** {@code spin:abstract} */
    public final static Property abstract_ = ResourceFactory.createProperty(NS + "abstract");
    
    /** {@code spin:body} */
    public final static Property body = ResourceFactory.createProperty(NS + "body");

    /** {@code spin:cachable} */
    public final static Property cachable = ResourceFactory.createProperty(NS + "cachable");

    /** {@code spin:column} */
    public final static Property column = ResourceFactory.createProperty(NS + "column");

    /** {@code spin:columnIndex} */
    public final static Property columnIndex = ResourceFactory.createProperty(NS + "columnIndex");

    /** {@code spin:columnWidth} */
    public final static Property columnWidth = ResourceFactory.createProperty(NS + "columnWidth");

    /** {@code spin:columnType} */
    public final static Property columnType = ResourceFactory.createProperty(NS + "columnType");
    
    /** {@code spin:command} */
    public final static Property command = ResourceFactory.createProperty(NS + "command");
    
    /** {@code spin:constraint} */
    public final static Property constraint = ResourceFactory.createProperty(NS + "constraint");
    
    /** {@code spin:constructor} */
    public final static Property constructor = ResourceFactory.createProperty(NS + "constructor");

    /** {@code spin:fix} */
    public final static Property fix = ResourceFactory.createProperty(NS + "fix");

    /** {@code spin:imports} */
    public final static Property imports = ResourceFactory.createProperty(NS + "imports");

    /** {@code spin:labelTemplate} */
    public final static Property labelTemplate = ResourceFactory.createProperty(NS + "labelTemplate");

    /** {@code spin:nextRuleProperty} */
    public final static Property nextRuleProperty = ResourceFactory.createProperty(NS + "nextRuleProperty");

    /** {@code spin:private} */
    public final static Property private_ = ResourceFactory.createProperty(NS + "private");

    /** {@code spin:query} */
    public final static Property query = ResourceFactory.createProperty(NS + "query");

    /** {@code spin:returnType} */
    public final static Property returnType = ResourceFactory.createProperty(NS + "returnType");
    
    /** {@code spin:rule} */
    public final static Property rule = ResourceFactory.createProperty(NS + "rule");

    /** {@code spin:rulePropertyMaxIterationCount} */
    public final static Property rulePropertyMaxIterationCount = ResourceFactory.createProperty(NS + "rulePropertyMaxIterationCount");

    /** {@code spin:symbol} */
    public final static Property symbol = ResourceFactory.createProperty(NS + "symbol");

    /** {@code spin:thisUnbound} */
    public final static Property thisUnbound = ResourceFactory.createProperty(NS + "thisUnbound");
    
    /** {@code spin:violationDetail} */
    public final static Property violationDetail = ResourceFactory.createProperty(NS + "violationDetail");
    
    /** {@code spin:violationLevel} */
    public final static Property violationLevel = ResourceFactory.createProperty(NS + "violationLevel");
    
    /** {@code spin:violationPath} */
    public final static Property violationPath = ResourceFactory.createProperty(NS + "violationPath");
    
    /** {@code spin:violationRoot} */
    public final static Property violationRoot = ResourceFactory.createProperty(NS + "violationRoot");
    
    /** {@code spin:violationSource} */
    public final static Property violationSource = ResourceFactory.createProperty(NS + "violationSource");
    
    /** {@code spin:violationValue} */
    public final static Property violationValue = ResourceFactory.createProperty(NS + "violationValue");
    

    /** {@code spin:_arg1} */
    public final static Resource _arg1 = ResourceFactory.createProperty(NS + "_arg1");

    /** {@code spin:_arg2} */
    public final static Resource _arg2 = ResourceFactory.createProperty(NS + "_arg2");

    /** {@code spin:_arg3} */
    public final static Resource _arg3 = ResourceFactory.createProperty(NS + "_arg3");

    /** {@code spin:_arg4} */
    public final static Resource _arg4 = ResourceFactory.createProperty(NS + "_arg4");

    /** {@code spin:_arg5} */
    public final static Resource _arg5 = ResourceFactory.createProperty(NS + "_arg5");
    
    /** {@code spin:_this} */
    public final static Resource _this = ResourceFactory.createResource(NS + "_this");
        
}
