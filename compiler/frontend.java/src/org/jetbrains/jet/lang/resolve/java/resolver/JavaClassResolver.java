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

package org.jetbrains.jet.lang.resolve.java.resolver;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import gnu.trove.THashMap;
import gnu.trove.TObjectHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.descriptors.serialization.ClassId;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.DescriptorResolver;
import org.jetbrains.jet.lang.resolve.DescriptorUtils;
import org.jetbrains.jet.lang.resolve.java.*;
import org.jetbrains.jet.lang.resolve.java.descriptor.ClassDescriptorFromJvmBytecode;
import org.jetbrains.jet.lang.resolve.java.jetAsJava.JetJavaMirrorMarker;
import org.jetbrains.jet.lang.resolve.java.sam.SingleAbstractMethodUtils;
import org.jetbrains.jet.lang.resolve.java.scope.JavaClassNonStaticMembersScope;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.java.structure.JavaMethod;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.FqNameBase;
import org.jetbrains.jet.lang.resolve.name.FqNameUnsafe;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.resolve.scopes.RedeclarationHandler;
import org.jetbrains.jet.lang.resolve.scopes.WritableScope;
import org.jetbrains.jet.lang.resolve.scopes.WritableScopeImpl;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeUtils;
import org.jetbrains.jet.lang.types.checker.JetTypeChecker;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import javax.inject.Inject;
import java.util.*;

import static org.jetbrains.jet.lang.resolve.DescriptorUtils.getClassObjectName;
import static org.jetbrains.jet.lang.resolve.java.resolver.DeserializedResolverUtils.getVirtualFile;

public final class JavaClassResolver {

    // NOTE: this complexity is introduced because class descriptors do not always have valid fqnames (class objects)
    @NotNull
    private final Map<FqNameBase, ClassDescriptor> classDescriptorCache =
            new THashMap<FqNameBase, ClassDescriptor>(new TObjectHashingStrategy<FqNameBase>() {
                @Override
                public int computeHashCode(FqNameBase o) {
                    if (o instanceof FqName) {
                        return ((FqName) o).toUnsafe().hashCode();
                    }
                    assert o instanceof FqNameUnsafe;
                    return o.hashCode();
                }

                @Override
                public boolean equals(FqNameBase n1, FqNameBase n2) {
                    return n1.equalsTo(n2.toString()) && n2.equalsTo(n1.toString());
                }
            });

    @NotNull
    private final Set<FqNameBase> unresolvedCache = Sets.newHashSet();

    private BindingTrace trace;
    private JavaResolverCache cache;
    private JavaTypeParameterResolver typeParameterResolver;
    private JavaDescriptorResolver javaDescriptorResolver;
    private JavaAnnotationResolver annotationResolver;
    private JavaClassFinder javaClassFinder;
    private JavaNamespaceResolver namespaceResolver;
    private JavaSupertypeResolver supertypesResolver;
    private JavaFunctionResolver functionResolver;
    private DeserializedDescriptorResolver kotlinDescriptorResolver;

    public JavaClassResolver() {
    }

    @Inject
    public void setTrace(BindingTrace trace) {
        this.trace = trace;
    }

    @Inject
    public void setCache(JavaResolverCache cache) {
        this.cache = cache;
    }

    @Inject
    public void setKotlinDescriptorResolver(DeserializedDescriptorResolver kotlinDescriptorResolver) {
        this.kotlinDescriptorResolver = kotlinDescriptorResolver;
    }

    @Inject
    public void setTypeParameterResolver(JavaTypeParameterResolver typeParameterResolver) {
        this.typeParameterResolver = typeParameterResolver;
    }

    @Inject
    public void setJavaDescriptorResolver(JavaDescriptorResolver javaDescriptorResolver) {
        this.javaDescriptorResolver = javaDescriptorResolver;
    }

    @Inject
    public void setAnnotationResolver(JavaAnnotationResolver annotationResolver) {
        this.annotationResolver = annotationResolver;
    }

    @Inject
    public void setJavaClassFinder(JavaClassFinder javaClassFinder) {
        this.javaClassFinder = javaClassFinder;
    }

    @Inject
    public void setNamespaceResolver(JavaNamespaceResolver namespaceResolver) {
        this.namespaceResolver = namespaceResolver;
    }

    @Inject
    public void setSupertypesResolver(JavaSupertypeResolver supertypesResolver) {
        this.supertypesResolver = supertypesResolver;
    }

    @Inject
    public void setFunctionResolver(JavaFunctionResolver functionResolver) {
        this.functionResolver = functionResolver;
    }

    @Nullable
    public ClassDescriptor resolveClass(@NotNull FqName qualifiedName, @NotNull DescriptorSearchRule searchRule) {
        PostponedTasks postponedTasks = new PostponedTasks();
        ClassDescriptor classDescriptor = resolveClass(qualifiedName, searchRule, postponedTasks);
        postponedTasks.performTasks();
        return classDescriptor;
    }

    @Nullable
    public ClassDescriptor resolveClass(
            @NotNull FqName qualifiedName,
            @NotNull DescriptorSearchRule searchRule,
            @NotNull PostponedTasks tasks
    ) {
        if (isTraitImplementation(qualifiedName)) {
            return null;
        }

        ClassDescriptor builtinClassDescriptor = getKotlinBuiltinClassDescriptor(qualifiedName);
        if (builtinClassDescriptor != null) {
            return builtinClassDescriptor;
        }

        // First, let's check that this is a real Java class, not a Java's view on a Kotlin class:
        ClassDescriptor kotlinClassDescriptor = trace.get(BindingContext.FQNAME_TO_CLASS_DESCRIPTOR, qualifiedName);
        if (kotlinClassDescriptor != null) {
            return searchRule.processFoundInKotlin(kotlinClassDescriptor);
        }

        // Not let's take a descriptor of a Java class
        FqNameUnsafe fqName = javaClassToKotlinFqName(qualifiedName);
        ClassDescriptor cachedDescriptor = classDescriptorCache.get(fqName);
        if (cachedDescriptor != null) {
            return cachedDescriptor;
        }

        if (unresolvedCache.contains(fqName)) {
            return null;
        }

        return doResolveClass(qualifiedName, tasks);
    }

    @Nullable
    private static ClassDescriptor getKotlinBuiltinClassDescriptor(@NotNull FqName qualifiedName) {
        if (!qualifiedName.firstSegmentIs(KotlinBuiltIns.BUILT_INS_PACKAGE_NAME)) return null;

        List<Name> segments = qualifiedName.pathSegments();
        if (segments.size() < 2) return null;

        JetScope scope = KotlinBuiltIns.getInstance().getBuiltInsScope();
        for (int i = 1, size = segments.size(); i < size; i++) {
            ClassifierDescriptor classifier = scope.getClassifier(segments.get(i));
            if (classifier == null) return null;
            assert classifier instanceof ClassDescriptor : "Unexpected classifier in built-ins: " + classifier;
            scope = ((ClassDescriptor) classifier).getUnsubstitutedInnerClassesScope();
        }

        return (ClassDescriptor) scope.getContainingDeclaration();
    }

    private ClassDescriptor doResolveClass(@NotNull FqName qualifiedName, @NotNull PostponedTasks tasks) {
        JavaClass javaClass = javaClassFinder.findClass(qualifiedName);
        if (javaClass == null) {
            cacheNegativeValue(javaClassToKotlinFqName(qualifiedName));
            return null;
        }

        // Class may have been resolved previously by different Java resolver instance, and we are reusing its trace
        ClassDescriptor alreadyResolved = cache.getClass(javaClass);
        if (alreadyResolved != null) {
            return alreadyResolved;
        }

        return createJavaClassDescriptor(qualifiedName, javaClass, tasks);
    }

    private void cacheNegativeValue(@NotNull FqNameBase qualifiedName) {
        if (unresolvedCache.contains(qualifiedName) || classDescriptorCache.containsKey(qualifiedName)) {
            throw new IllegalStateException("rewrite at " + qualifiedName);
        }
        unresolvedCache.add(qualifiedName);
    }

    private static boolean isTraitImplementation(@NotNull FqName qualifiedName) {
        // TODO: only if -$$TImpl class is created by Kotlin
        return qualifiedName.asString().endsWith(JvmAbi.TRAIT_IMPL_SUFFIX);
    }

    @NotNull
    private ClassDescriptor createJavaClassDescriptor(@NotNull FqName fqName, @NotNull JavaClass javaClass, @NotNull PostponedTasks taskList) {
        checkFqNamesAreConsistent(javaClass, fqName);
        checkPsiClassIsNotJet(javaClass);

        ClassOrNamespaceDescriptor containingDeclaration = resolveParentDescriptor(javaClass);
        // class may be resolved during resolution of parent
        ClassDescriptor cachedDescriptor = classDescriptorCache.get(javaClassToKotlinFqName(fqName));
        if (cachedDescriptor != null) {
            return cachedDescriptor;
        }

        assert (!unresolvedCache.contains(fqName)) : "We can resolve the class, so it can't be 'unresolved' during parent resolution";

        VirtualFile outerClassFile = javaClass.getPsi().getContainingFile().getVirtualFile();
        if (outerClassFile != null) {
            ClassId id = ClassId.fromFqNameAndContainingDeclaration(fqName, containingDeclaration);
            VirtualFile file = getVirtualFile(id, outerClassFile);
            if (file != null) {
                ClassDescriptor deserializedDescriptor = kotlinDescriptorResolver.resolveClass(id, file,
                        DescriptorResolverUtils.createPsiBasedErrorReporter(javaClass.getPsi(), trace));

                if (deserializedDescriptor != null) {
                    //TODO: class object and psi class
                    cache(javaClassToKotlinFqName(fqName), deserializedDescriptor);
                    return deserializedDescriptor;
                }
            }
        }

        return doCreateClassDescriptor(fqName, javaClass, taskList, containingDeclaration);
    }

    @NotNull
    private ClassDescriptorFromJvmBytecode doCreateClassDescriptor(
            @NotNull FqName fqName,
            @NotNull JavaClass javaClass,
            @NotNull PostponedTasks taskList,
            @NotNull ClassOrNamespaceDescriptor containingDeclaration
    ) {
        ClassDescriptorFromJvmBytecode classDescriptor = new ClassDescriptorFromJvmBytecode(containingDeclaration, javaClass.getKind(),
                                                                                            isInnerClass(javaClass));

        cache(javaClassToKotlinFqName(fqName), classDescriptor);

        classDescriptor.setName(javaClass.getName());

        JavaTypeParameterResolver.Initializer typeParameterInitializer = typeParameterResolver.resolveTypeParameters(classDescriptor, javaClass);
        classDescriptor.setTypeParameterDescriptors(typeParameterInitializer.getDescriptors());

        List<JetType> supertypes = Lists.newArrayList();
        classDescriptor.setSupertypes(supertypes);
        classDescriptor.setVisibility(javaClass.getVisibility());
        classDescriptor.setModality(javaClass.getModality());
        classDescriptor.createTypeConstructor();

        JavaClassNonStaticMembersScope scope = new JavaClassNonStaticMembersScope(classDescriptor, javaClass, false, javaDescriptorResolver);
        classDescriptor.setScopeForMemberLookup(scope);
        classDescriptor.setScopeForConstructorResolve(scope);

        typeParameterInitializer.initialize();

        // TODO: ugly hack: tests crash if initializeTypeParameters called with class containing proper supertypes
        List<TypeParameterDescriptor> classTypeParameters = classDescriptor.getTypeConstructor().getParameters();
        supertypes.addAll(supertypesResolver.getSupertypes(classDescriptor, javaClass, classTypeParameters));

        if (javaClass.isEnum()) {
            ClassDescriptorFromJvmBytecode classObjectDescriptor = createClassObjectDescriptorForEnum(classDescriptor, javaClass);
            cache(getFqNameForClassObject(javaClass), classObjectDescriptor);
            classDescriptor.getBuilder().setClassObjectDescriptor(classObjectDescriptor);
        }

        classDescriptor.setAnnotations(annotationResolver.resolveAnnotations(javaClass, taskList));

        cache.recordClass(javaClass, classDescriptor);

        JavaMethod samInterfaceMethod = SingleAbstractMethodUtils.getSamInterfaceMethod(javaClass);
        if (samInterfaceMethod != null) {
            SimpleFunctionDescriptor abstractMethod = resolveFunctionOfSamInterface(samInterfaceMethod, classDescriptor);
            classDescriptor.setFunctionTypeForSamInterface(SingleAbstractMethodUtils.getFunctionTypeForAbstractMethod(abstractMethod));
        }

        return classDescriptor;
    }

    @NotNull
    private static FqNameUnsafe getFqNameForClassObject(@NotNull JavaClass javaClass) {
        FqName fqName = javaClass.getFqName();
        assert fqName != null : "Reading java class with no qualified name";
        return fqName.toUnsafe().child(getClassObjectName(javaClass.getName()));
    }

    @NotNull
    private SimpleFunctionDescriptor resolveFunctionOfSamInterface(
            @NotNull JavaMethod samInterfaceMethod,
            @NotNull ClassDescriptorFromJvmBytecode samInterface
    ) {
        JavaClass methodContainer = samInterfaceMethod.getContainingClass();
        FqName containerFqName = methodContainer.getFqName();
        assert containerFqName != null : "qualified name is null for " + methodContainer;

        if (DescriptorUtils.getFQName(samInterface).equalsTo(containerFqName)) {
            SimpleFunctionDescriptor abstractMethod = functionResolver.resolveFunctionMutely(samInterfaceMethod, samInterface);
            assert abstractMethod != null : "couldn't resolve method " + samInterfaceMethod;
            return abstractMethod;
        }
        else {
            return findFunctionWithMostSpecificReturnType(TypeUtils.getAllSupertypes(samInterface.getDefaultType()));
        }
    }

    @NotNull
    private static SimpleFunctionDescriptor findFunctionWithMostSpecificReturnType(@NotNull Set<JetType> supertypes) {
        List<SimpleFunctionDescriptor> candidates = Lists.newArrayList();
        for (JetType supertype : supertypes) {
            List<CallableMemberDescriptor> abstractMembers = SingleAbstractMethodUtils.getAbstractMembers(supertype);
            if (!abstractMembers.isEmpty()) {
                candidates.add((SimpleFunctionDescriptor) abstractMembers.get(0));
            }
        }
        if (candidates.isEmpty()) {
            throw new IllegalStateException("Couldn't find abstract method in supertypes " + supertypes);
        }
        SimpleFunctionDescriptor currentMostSpecificType = candidates.get(0);
        for (SimpleFunctionDescriptor candidate : candidates) {
            JetType candidateReturnType = candidate.getReturnType();
            JetType currentMostSpecificReturnType = currentMostSpecificType.getReturnType();
            assert candidateReturnType != null && currentMostSpecificReturnType != null : candidate + ", " + currentMostSpecificReturnType;
            if (JetTypeChecker.INSTANCE.isSubtypeOf(candidateReturnType, currentMostSpecificReturnType)) {
                currentMostSpecificType = candidate;
            }
        }
        return currentMostSpecificType;
    }

    private void cache(@NotNull FqNameBase fqName, @Nullable ClassDescriptor classDescriptor) {
        if (classDescriptor == null) {
            cacheNegativeValue(fqName);
        }
        else {
            ClassDescriptor oldValue = classDescriptorCache.put(fqName, classDescriptor);
            assert oldValue == null;
        }
    }

    private void checkFqNamesAreConsistent(@NotNull JavaClass javaClass, @NotNull FqName desiredFqName) {
        FqName fqName = javaClass.getFqName();
        assert desiredFqName.equals(fqName) : "Inconsistent FQ names: " + fqName + ", " + desiredFqName;
        FqNameUnsafe correctedName = javaClassToKotlinFqName(fqName);
        if (classDescriptorCache.containsKey(correctedName) || unresolvedCache.contains(correctedName)) {
            throw new IllegalStateException("Cache already contains FQ name: " + fqName.asString());
        }
    }

    private static void checkPsiClassIsNotJet(@NotNull JavaClass javaClass) {
        if (javaClass.getPsi() instanceof JetJavaMirrorMarker) {
            throw new IllegalStateException("trying to resolve fake jet PsiClass as regular PsiClass: " + javaClass.getFqName());
        }
    }

    @NotNull
    private ClassOrNamespaceDescriptor resolveParentDescriptor(@NotNull JavaClass javaClass) {
        JavaClass outer = javaClass.getOuterClass();
        if (outer != null) {
            return resolveParentClass(javaClass, outer);
        }
        else {
            return resolveParentNamespace(javaClass);
        }
    }

    // This method replaces "object" segments of FQ name to "<class-object-for-...>"
    @NotNull
    private static FqNameUnsafe javaClassToKotlinFqName(@NotNull FqName rawFqName) {
        List<Name> correctedSegments = new ArrayList<Name>();
        for (Name segment : rawFqName.pathSegments()) {
            if (JvmAbi.CLASS_OBJECT_CLASS_NAME.equals(segment.asString())) {
                assert !correctedSegments.isEmpty();
                Name previous = correctedSegments.get(correctedSegments.size() - 1);
                correctedSegments.add(DescriptorUtils.getClassObjectName(previous));
            }
            else {
                correctedSegments.add(segment);
            }
        }
        return new FqNameUnsafe(StringUtil.join(correctedSegments, "."));
    }

    private static boolean isInnerClass(@NotNull JavaClass javaClass) {
        return javaClass.getOuterClass() != null && !javaClass.isStatic();
    }

    @NotNull
    private ClassDescriptor resolveParentClass(@NotNull JavaClass javaClass, @NotNull JavaClass container) {
        FqName containerFqName = container.getFqName();
        assert containerFqName != null : "Outer class has no FQ name: " + javaClass + " contained in " + container;
        ClassDescriptor parentClass = resolveClass(containerFqName, DescriptorSearchRule.INCLUDE_KOTLIN);
        if (parentClass == null) {
            throw new IllegalStateException("PsiClass not found by name " + containerFqName +
                                            ", required to be container declaration of " + javaClass.getFqName());
        }
        return parentClass;
    }

    @NotNull
    private NamespaceDescriptor resolveParentNamespace(@NotNull JavaClass javaClass) {
        FqName fqName = javaClass.getFqName();
        assert fqName != null : "Class has no FQ name: " + javaClass;
        FqName namespaceFqName = fqName.parent();
        NamespaceDescriptor parentNamespace = namespaceResolver.resolveNamespace(namespaceFqName, DescriptorSearchRule.INCLUDE_KOTLIN);
        if (parentNamespace == null) {
            throw new IllegalStateException("Cannot resolve namespace " + namespaceFqName + ", required to be container for " + fqName);
        }
        return parentNamespace;
    }

    @Nullable
    public ClassDescriptor resolveClass(FqName name) {
        return resolveClass(name, DescriptorSearchRule.ERROR_IF_FOUND_IN_KOTLIN);
    }

    @NotNull
    private ClassDescriptorFromJvmBytecode createClassObjectDescriptorForEnum(
            @NotNull ClassDescriptor containing,
            @NotNull JavaClass javaClass
    ) {
        ClassDescriptorFromJvmBytecode classObjectDescriptor = createSyntheticClassObject(containing, javaClass);

        JetType valuesReturnType = KotlinBuiltIns.getInstance().getArrayType(containing.getDefaultType());
        SimpleFunctionDescriptor valuesMethod =
                DescriptorResolver.createEnumClassObjectValuesMethod(classObjectDescriptor, valuesReturnType);
        classObjectDescriptor.getBuilder().addFunctionDescriptor(valuesMethod);

        JetType valueOfReturnType = containing.getDefaultType();
        SimpleFunctionDescriptor valueOfMethod =
                DescriptorResolver.createEnumClassObjectValueOfMethod(classObjectDescriptor, valueOfReturnType);
        classObjectDescriptor.getBuilder().addFunctionDescriptor(valueOfMethod);

        return classObjectDescriptor;
    }

    @NotNull
    private ClassDescriptorFromJvmBytecode createSyntheticClassObject(@NotNull ClassDescriptor containing, @NotNull JavaClass javaClass) {
        ClassDescriptorFromJvmBytecode classObjectDescriptor =
                new ClassDescriptorFromJvmBytecode(containing, ClassKind.CLASS_OBJECT, false);

        classObjectDescriptor.setName(getClassObjectName(containing.getName()));
        classObjectDescriptor.setModality(Modality.FINAL);
        classObjectDescriptor.setVisibility(containing.getVisibility());
        classObjectDescriptor.setTypeParameterDescriptors(Collections.<TypeParameterDescriptor>emptyList());
        classObjectDescriptor.createTypeConstructor();

        JavaClassNonStaticMembersScope scope = new JavaClassNonStaticMembersScope(classObjectDescriptor, javaClass, true,
                                                                                  javaDescriptorResolver);
        WritableScopeImpl writableScope =
                new WritableScopeImpl(scope, classObjectDescriptor, RedeclarationHandler.THROW_EXCEPTION, "Member lookup scope");
        writableScope.changeLockLevel(WritableScope.LockLevel.BOTH);
        classObjectDescriptor.setScopeForMemberLookup(writableScope);
        classObjectDescriptor.setScopeForConstructorResolve(scope);

        return classObjectDescriptor;
    }
}
