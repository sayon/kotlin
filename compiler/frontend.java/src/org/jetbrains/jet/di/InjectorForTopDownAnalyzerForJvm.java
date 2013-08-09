/*
 * Copyright 2010-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.di;

import org.jetbrains.jet.lang.resolve.TopDownAnalyzer;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisContext;
import org.jetbrains.jet.lang.resolve.BodyResolver;
import org.jetbrains.jet.lang.resolve.ControlFlowAnalyzer;
import org.jetbrains.jet.lang.resolve.DeclarationsChecker;
import org.jetbrains.jet.lang.resolve.DescriptorResolver;
import org.jetbrains.jet.lang.resolve.calls.NeedSyntheticCallResolverExtension;
import com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.resolve.TopDownAnalysisParameters;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.descriptors.ModuleDescriptorImpl;
import org.jetbrains.jet.lang.resolve.java.JavaBridgeConfiguration;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.PsiClassFinderImpl;
import org.jetbrains.jet.lang.resolve.java.JavaClassFinderImpl;
import org.jetbrains.jet.lang.resolve.java.resolver.TraceBasedExternalSignatureResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.TraceBasedJavaResolverCache;
import org.jetbrains.jet.lang.resolve.java.resolver.TraceBasedErrorReporterProvider;
import org.jetbrains.jet.lang.resolve.java.resolver.PsiBasedMethodSignatureChecker;
import org.jetbrains.jet.lang.resolve.java.resolver.PsiBasedExternalAnnotationResolver;
import org.jetbrains.jet.lang.resolve.NamespaceFactoryImpl;
import org.jetbrains.jet.lang.resolve.DeclarationResolver;
import org.jetbrains.jet.lang.resolve.AnnotationResolver;
import org.jetbrains.jet.lang.resolve.calls.CallResolver;
import org.jetbrains.jet.lang.resolve.calls.ArgumentTypeResolver;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingServices;
import org.jetbrains.jet.lang.resolve.calls.CallExpressionResolver;
import org.jetbrains.jet.lang.resolve.TypeResolver;
import org.jetbrains.jet.lang.resolve.QualifiedExpressionResolver;
import org.jetbrains.jet.lang.resolve.calls.CandidateResolver;
import org.jetbrains.jet.lang.resolve.ImportsResolver;
import org.jetbrains.jet.lang.psi.JetImportsFactory;
import org.jetbrains.jet.lang.resolve.ScriptHeaderResolver;
import org.jetbrains.jet.lang.resolve.OverloadResolver;
import org.jetbrains.jet.lang.resolve.OverrideResolver;
import org.jetbrains.jet.lang.resolve.TypeHierarchyResolver;
import org.jetbrains.jet.lang.resolve.ScriptBodyResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaClassResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaAnnotationResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaAnnotationArgumentResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaFunctionResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaTypeParameterResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaTypeTransformer;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaValueParameterResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.DeserializedDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.AnnotationDescriptorDeserializer;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaNamespaceResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaMemberResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaConstructorResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaPropertyResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaSupertypeResolver;
import org.jetbrains.annotations.NotNull;
import javax.annotation.PreDestroy;

/* This file is generated by org.jetbrains.jet.generators.injectors.GenerateInjectors. DO NOT EDIT! */
public class InjectorForTopDownAnalyzerForJvm implements InjectorForTopDownAnalyzer {
    
    private TopDownAnalyzer topDownAnalyzer;
    private TopDownAnalysisContext topDownAnalysisContext;
    private BodyResolver bodyResolver;
    private ControlFlowAnalyzer controlFlowAnalyzer;
    private DeclarationsChecker declarationsChecker;
    private DescriptorResolver descriptorResolver;
    private NeedSyntheticCallResolverExtension needSyntheticCallResolverExtension;
    private final Project project;
    private final TopDownAnalysisParameters topDownAnalysisParameters;
    private final BindingTrace bindingTrace;
    private final ModuleDescriptorImpl moduleDescriptor;
    private JavaBridgeConfiguration javaBridgeConfiguration;
    private JavaDescriptorResolver javaDescriptorResolver;
    private PsiClassFinderImpl psiClassFinder;
    private JavaClassFinderImpl javaClassFinder;
    private TraceBasedExternalSignatureResolver traceBasedExternalSignatureResolver;
    private TraceBasedJavaResolverCache traceBasedJavaResolverCache;
    private TraceBasedErrorReporterProvider traceBasedErrorReporterProvider;
    private PsiBasedMethodSignatureChecker psiBasedMethodSignatureChecker;
    private PsiBasedExternalAnnotationResolver psiBasedExternalAnnotationResolver;
    private NamespaceFactoryImpl namespaceFactory;
    private DeclarationResolver declarationResolver;
    private AnnotationResolver annotationResolver;
    private CallResolver callResolver;
    private ArgumentTypeResolver argumentTypeResolver;
    private ExpressionTypingServices expressionTypingServices;
    private CallExpressionResolver callExpressionResolver;
    private TypeResolver typeResolver;
    private QualifiedExpressionResolver qualifiedExpressionResolver;
    private CandidateResolver candidateResolver;
    private ImportsResolver importsResolver;
    private JetImportsFactory jetImportsFactory;
    private ScriptHeaderResolver scriptHeaderResolver;
    private OverloadResolver overloadResolver;
    private OverrideResolver overrideResolver;
    private TypeHierarchyResolver typeHierarchyResolver;
    private ScriptBodyResolver scriptBodyResolver;
    private JavaClassResolver javaClassResolver;
    private JavaAnnotationResolver javaAnnotationResolver;
    private JavaAnnotationArgumentResolver javaAnnotationArgumentResolver;
    private JavaFunctionResolver javaFunctionResolver;
    private JavaTypeParameterResolver javaTypeParameterResolver;
    private JavaTypeTransformer javaTypeTransformer;
    private JavaValueParameterResolver javaValueParameterResolver;
    private DeserializedDescriptorResolver deserializedDescriptorResolver;
    private AnnotationDescriptorDeserializer annotationDescriptorDeserializer;
    private JavaNamespaceResolver javaNamespaceResolver;
    private JavaMemberResolver javaMemberResolver;
    private JavaConstructorResolver javaConstructorResolver;
    private JavaPropertyResolver javaPropertyResolver;
    private JavaSupertypeResolver javaSupertypeResolver;
    
    public InjectorForTopDownAnalyzerForJvm(
        @NotNull Project project,
        @NotNull TopDownAnalysisParameters topDownAnalysisParameters,
        @NotNull BindingTrace bindingTrace,
        @NotNull ModuleDescriptorImpl moduleDescriptor
    ) {
        this.topDownAnalyzer = new TopDownAnalyzer();
        this.topDownAnalysisContext = new TopDownAnalysisContext();
        this.bodyResolver = new BodyResolver();
        this.controlFlowAnalyzer = new ControlFlowAnalyzer();
        this.declarationsChecker = new DeclarationsChecker();
        this.descriptorResolver = new DescriptorResolver();
        this.needSyntheticCallResolverExtension = new NeedSyntheticCallResolverExtension();
        this.project = project;
        this.topDownAnalysisParameters = topDownAnalysisParameters;
        this.bindingTrace = bindingTrace;
        this.moduleDescriptor = moduleDescriptor;
        this.javaBridgeConfiguration = new JavaBridgeConfiguration();
        this.javaDescriptorResolver = new JavaDescriptorResolver();
        this.psiClassFinder = new PsiClassFinderImpl();
        this.javaClassFinder = new JavaClassFinderImpl();
        this.traceBasedExternalSignatureResolver = new TraceBasedExternalSignatureResolver();
        this.traceBasedJavaResolverCache = new TraceBasedJavaResolverCache();
        this.traceBasedErrorReporterProvider = new TraceBasedErrorReporterProvider();
        this.psiBasedMethodSignatureChecker = new PsiBasedMethodSignatureChecker();
        this.psiBasedExternalAnnotationResolver = new PsiBasedExternalAnnotationResolver();
        this.namespaceFactory = new NamespaceFactoryImpl();
        this.declarationResolver = new DeclarationResolver();
        this.annotationResolver = new AnnotationResolver();
        this.callResolver = new CallResolver();
        this.argumentTypeResolver = new ArgumentTypeResolver();
        this.expressionTypingServices = new ExpressionTypingServices();
        this.callExpressionResolver = new CallExpressionResolver();
        this.typeResolver = new TypeResolver();
        this.qualifiedExpressionResolver = new QualifiedExpressionResolver();
        this.candidateResolver = new CandidateResolver();
        this.importsResolver = new ImportsResolver();
        this.jetImportsFactory = new JetImportsFactory();
        this.scriptHeaderResolver = new ScriptHeaderResolver();
        this.overloadResolver = new OverloadResolver();
        this.overrideResolver = new OverrideResolver();
        this.typeHierarchyResolver = new TypeHierarchyResolver();
        this.scriptBodyResolver = new ScriptBodyResolver();
        this.javaClassResolver = new JavaClassResolver();
        this.javaAnnotationResolver = new JavaAnnotationResolver();
        this.javaAnnotationArgumentResolver = new JavaAnnotationArgumentResolver();
        this.javaFunctionResolver = new JavaFunctionResolver();
        this.javaTypeParameterResolver = new JavaTypeParameterResolver();
        this.javaTypeTransformer = new JavaTypeTransformer();
        this.javaValueParameterResolver = new JavaValueParameterResolver();
        this.deserializedDescriptorResolver = new DeserializedDescriptorResolver();
        this.annotationDescriptorDeserializer = new AnnotationDescriptorDeserializer();
        this.javaNamespaceResolver = new JavaNamespaceResolver();
        this.javaMemberResolver = new JavaMemberResolver();
        this.javaConstructorResolver = new JavaConstructorResolver();
        this.javaPropertyResolver = new JavaPropertyResolver();
        this.javaSupertypeResolver = new JavaSupertypeResolver();

        this.topDownAnalyzer.setBodyResolver(bodyResolver);
        this.topDownAnalyzer.setContext(topDownAnalysisContext);
        this.topDownAnalyzer.setDeclarationResolver(declarationResolver);
        this.topDownAnalyzer.setModuleDescriptor(moduleDescriptor);
        this.topDownAnalyzer.setNamespaceFactory(namespaceFactory);
        this.topDownAnalyzer.setOverloadResolver(overloadResolver);
        this.topDownAnalyzer.setOverrideResolver(overrideResolver);
        this.topDownAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.topDownAnalyzer.setTrace(bindingTrace);
        this.topDownAnalyzer.setTypeHierarchyResolver(typeHierarchyResolver);

        this.topDownAnalysisContext.setTopDownAnalysisParameters(topDownAnalysisParameters);

        this.bodyResolver.setAnnotationResolver(annotationResolver);
        this.bodyResolver.setCallResolver(callResolver);
        this.bodyResolver.setContext(topDownAnalysisContext);
        this.bodyResolver.setControlFlowAnalyzer(controlFlowAnalyzer);
        this.bodyResolver.setDeclarationsChecker(declarationsChecker);
        this.bodyResolver.setDescriptorResolver(descriptorResolver);
        this.bodyResolver.setExpressionTypingServices(expressionTypingServices);
        this.bodyResolver.setScriptBodyResolverResolver(scriptBodyResolver);
        this.bodyResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.bodyResolver.setTrace(bindingTrace);

        this.controlFlowAnalyzer.setTopDownAnalysisParameters(topDownAnalysisParameters);
        this.controlFlowAnalyzer.setTrace(bindingTrace);

        this.declarationsChecker.setTrace(bindingTrace);

        this.descriptorResolver.setAnnotationResolver(annotationResolver);
        this.descriptorResolver.setExpressionTypingServices(expressionTypingServices);
        this.descriptorResolver.setTypeResolver(typeResolver);

        this.javaBridgeConfiguration.setJavaDescriptorResolver(javaDescriptorResolver);

        javaDescriptorResolver.setClassResolver(javaClassResolver);
        javaDescriptorResolver.setNamespaceResolver(javaNamespaceResolver);

        psiClassFinder.setProject(project);

        javaClassFinder.setPsiClassFinder(psiClassFinder);

        traceBasedExternalSignatureResolver.setAnnotationResolver(javaAnnotationResolver);
        traceBasedExternalSignatureResolver.setTrace(bindingTrace);

        traceBasedJavaResolverCache.setTrace(bindingTrace);

        traceBasedErrorReporterProvider.setTrace(bindingTrace);

        psiBasedMethodSignatureChecker.setAnnotationResolver(javaAnnotationResolver);
        psiBasedMethodSignatureChecker.setExternalSignatureResolver(traceBasedExternalSignatureResolver);

        this.namespaceFactory.setModuleDescriptor(moduleDescriptor);
        this.namespaceFactory.setTrace(bindingTrace);

        declarationResolver.setAnnotationResolver(annotationResolver);
        declarationResolver.setContext(topDownAnalysisContext);
        declarationResolver.setDescriptorResolver(descriptorResolver);
        declarationResolver.setImportsResolver(importsResolver);
        declarationResolver.setScriptHeaderResolver(scriptHeaderResolver);
        declarationResolver.setTrace(bindingTrace);

        annotationResolver.setCallResolver(callResolver);
        annotationResolver.setExpressionTypingServices(expressionTypingServices);

        callResolver.setArgumentTypeResolver(argumentTypeResolver);
        callResolver.setCandidateResolver(candidateResolver);
        callResolver.setExpressionTypingServices(expressionTypingServices);
        callResolver.setExtension(needSyntheticCallResolverExtension);
        callResolver.setTypeResolver(typeResolver);

        argumentTypeResolver.setExpressionTypingServices(expressionTypingServices);
        argumentTypeResolver.setTypeResolver(typeResolver);

        expressionTypingServices.setCallExpressionResolver(callExpressionResolver);
        expressionTypingServices.setCallResolver(callResolver);
        expressionTypingServices.setDescriptorResolver(descriptorResolver);
        expressionTypingServices.setProject(project);
        expressionTypingServices.setTypeResolver(typeResolver);

        callExpressionResolver.setExpressionTypingServices(expressionTypingServices);

        typeResolver.setAnnotationResolver(annotationResolver);
        typeResolver.setDescriptorResolver(descriptorResolver);
        typeResolver.setModuleDescriptor(moduleDescriptor);
        typeResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);

        candidateResolver.setArgumentTypeResolver(argumentTypeResolver);

        importsResolver.setContext(topDownAnalysisContext);
        importsResolver.setImportsFactory(jetImportsFactory);
        importsResolver.setModuleDescriptor(moduleDescriptor);
        importsResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);
        importsResolver.setTrace(bindingTrace);

        jetImportsFactory.setProject(project);

        scriptHeaderResolver.setContext(topDownAnalysisContext);
        scriptHeaderResolver.setDependencyClassByQualifiedNameResolver(javaDescriptorResolver);
        scriptHeaderResolver.setNamespaceFactory(namespaceFactory);
        scriptHeaderResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        scriptHeaderResolver.setTrace(bindingTrace);

        overloadResolver.setContext(topDownAnalysisContext);
        overloadResolver.setTrace(bindingTrace);

        overrideResolver.setContext(topDownAnalysisContext);
        overrideResolver.setTopDownAnalysisParameters(topDownAnalysisParameters);
        overrideResolver.setTrace(bindingTrace);

        typeHierarchyResolver.setContext(topDownAnalysisContext);
        typeHierarchyResolver.setDescriptorResolver(descriptorResolver);
        typeHierarchyResolver.setImportsResolver(importsResolver);
        typeHierarchyResolver.setNamespaceFactory(namespaceFactory);
        typeHierarchyResolver.setScriptHeaderResolver(scriptHeaderResolver);
        typeHierarchyResolver.setTrace(bindingTrace);

        scriptBodyResolver.setContext(topDownAnalysisContext);
        scriptBodyResolver.setExpressionTypingServices(expressionTypingServices);
        scriptBodyResolver.setTrace(bindingTrace);

        javaClassResolver.setAnnotationResolver(javaAnnotationResolver);
        javaClassResolver.setCache(traceBasedJavaResolverCache);
        javaClassResolver.setErrorReporterProvider(traceBasedErrorReporterProvider);
        javaClassResolver.setFunctionResolver(javaFunctionResolver);
        javaClassResolver.setJavaClassFinder(javaClassFinder);
        javaClassResolver.setKotlinDescriptorResolver(deserializedDescriptorResolver);
        javaClassResolver.setMemberResolver(javaMemberResolver);
        javaClassResolver.setNamespaceResolver(javaNamespaceResolver);
        javaClassResolver.setSupertypesResolver(javaSupertypeResolver);
        javaClassResolver.setTypeParameterResolver(javaTypeParameterResolver);

        javaAnnotationResolver.setArgumentResolver(javaAnnotationArgumentResolver);
        javaAnnotationResolver.setClassResolver(javaClassResolver);
        javaAnnotationResolver.setExternalAnnotationResolver(psiBasedExternalAnnotationResolver);

        javaAnnotationArgumentResolver.setAnnotationResolver(javaAnnotationResolver);
        javaAnnotationArgumentResolver.setClassResolver(javaClassResolver);

        javaFunctionResolver.setAnnotationResolver(javaAnnotationResolver);
        javaFunctionResolver.setCache(traceBasedJavaResolverCache);
        javaFunctionResolver.setExternalSignatureResolver(traceBasedExternalSignatureResolver);
        javaFunctionResolver.setSignatureChecker(psiBasedMethodSignatureChecker);
        javaFunctionResolver.setTypeParameterResolver(javaTypeParameterResolver);
        javaFunctionResolver.setTypeTransformer(javaTypeTransformer);
        javaFunctionResolver.setValueParameterResolver(javaValueParameterResolver);

        javaTypeParameterResolver.setTypeTransformer(javaTypeTransformer);

        javaTypeTransformer.setClassResolver(javaClassResolver);

        javaValueParameterResolver.setAnnotationResolver(javaAnnotationResolver);
        javaValueParameterResolver.setTypeTransformer(javaTypeTransformer);

        deserializedDescriptorResolver.setAnnotationDeserializer(annotationDescriptorDeserializer);
        deserializedDescriptorResolver.setJavaClassResolver(javaClassResolver);
        deserializedDescriptorResolver.setJavaNamespaceResolver(javaNamespaceResolver);

        annotationDescriptorDeserializer.setJavaClassResolver(javaClassResolver);
        annotationDescriptorDeserializer.setPsiClassFinder(psiClassFinder);

        javaNamespaceResolver.setCache(traceBasedJavaResolverCache);
        javaNamespaceResolver.setDeserializedDescriptorResolver(deserializedDescriptorResolver);
        javaNamespaceResolver.setErrorReporterProvider(traceBasedErrorReporterProvider);
        javaNamespaceResolver.setJavaClassFinder(javaClassFinder);
        javaNamespaceResolver.setMemberResolver(javaMemberResolver);

        javaMemberResolver.setClassResolver(javaClassResolver);
        javaMemberResolver.setConstructorResolver(javaConstructorResolver);
        javaMemberResolver.setFunctionResolver(javaFunctionResolver);
        javaMemberResolver.setNamespaceResolver(javaNamespaceResolver);
        javaMemberResolver.setPropertyResolver(javaPropertyResolver);

        javaConstructorResolver.setCache(traceBasedJavaResolverCache);
        javaConstructorResolver.setExternalSignatureResolver(traceBasedExternalSignatureResolver);
        javaConstructorResolver.setTypeTransformer(javaTypeTransformer);
        javaConstructorResolver.setValueParameterResolver(javaValueParameterResolver);

        javaPropertyResolver.setAnnotationResolver(javaAnnotationResolver);
        javaPropertyResolver.setCache(traceBasedJavaResolverCache);
        javaPropertyResolver.setExternalSignatureResolver(traceBasedExternalSignatureResolver);
        javaPropertyResolver.setTypeTransformer(javaTypeTransformer);

        javaSupertypeResolver.setClassResolver(javaClassResolver);
        javaSupertypeResolver.setTypeTransformer(javaTypeTransformer);

        psiClassFinder.initialize();

    }
    
    @PreDestroy
    public void destroy() {
    }
    
    public TopDownAnalyzer getTopDownAnalyzer() {
        return this.topDownAnalyzer;
    }
    
    public TopDownAnalysisContext getTopDownAnalysisContext() {
        return this.topDownAnalysisContext;
    }
    
    public BodyResolver getBodyResolver() {
        return this.bodyResolver;
    }
    
    public ControlFlowAnalyzer getControlFlowAnalyzer() {
        return this.controlFlowAnalyzer;
    }
    
    public DeclarationsChecker getDeclarationsChecker() {
        return this.declarationsChecker;
    }
    
    public DescriptorResolver getDescriptorResolver() {
        return this.descriptorResolver;
    }
    
    public Project getProject() {
        return this.project;
    }
    
    public TopDownAnalysisParameters getTopDownAnalysisParameters() {
        return this.topDownAnalysisParameters;
    }
    
    public BindingTrace getBindingTrace() {
        return this.bindingTrace;
    }
    
    public ModuleDescriptorImpl getModuleDescriptor() {
        return this.moduleDescriptor;
    }
    
    public JavaBridgeConfiguration getJavaBridgeConfiguration() {
        return this.javaBridgeConfiguration;
    }
    
    public NamespaceFactoryImpl getNamespaceFactory() {
        return this.namespaceFactory;
    }
    
}
