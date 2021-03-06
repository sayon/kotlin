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

package org.jetbrains.jet.lang.resolve.java.scope;

import com.google.common.collect.Sets;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.java.DescriptorResolverUtils;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.java.JetJavaMirrorMarker;
import org.jetbrains.jet.lang.resolve.java.PsiClassFinder;
import org.jetbrains.jet.lang.resolve.java.provider.NamedMembers;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule.IGNORE_KOTLIN_SOURCES;
import static org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule.INCLUDE_KOTLIN_SOURCES;

public final class JavaPackageScope extends JavaBaseScope {
    @NotNull
    private final PsiPackage psiPackage;
    @NotNull
    private final FqName packageFQN;
    @NotNull
    private final PsiClassFinder psiClassFinder;

    public JavaPackageScope(
            @NotNull NamespaceDescriptor descriptor,
            @NotNull PsiPackage psiPackage,
            @NotNull FqName packageFQN,
            @NotNull JavaDescriptorResolver javaDescriptorResolver,
            @NotNull PsiClassFinder psiClassFinder
    ) {
        super(descriptor, javaDescriptorResolver, MembersProvider.forPackage(psiClassFinder, psiPackage));
        this.psiPackage = psiPackage;
        this.packageFQN = packageFQN;
        this.psiClassFinder = psiClassFinder;
    }

    @Override
    @NotNull
    public PsiPackage getPsiElement() {
        return psiPackage;
    }

    @Override
    public ClassifierDescriptor getClassifier(@NotNull Name name) {
        ClassDescriptor classDescriptor = javaDescriptorResolver.resolveClass(packageFQN.child(name), IGNORE_KOTLIN_SOURCES);
        if (classDescriptor == null || classDescriptor.getKind().isObject()) {
            return null;
        }
        return classDescriptor;
    }

    @Override
    public ClassDescriptor getObjectDescriptor(@NotNull Name name) {
        ClassDescriptor classDescriptor = javaDescriptorResolver.resolveClass(packageFQN.child(name), IGNORE_KOTLIN_SOURCES);
        if (classDescriptor != null && classDescriptor.getKind().isObject()) {
            return classDescriptor;
        }
        return null;
    }

    @Override
    public NamespaceDescriptor getNamespace(@NotNull Name name) {
        return javaDescriptorResolver.resolveNamespace(packageFQN.child(name), INCLUDE_KOTLIN_SOURCES);
    }

    @NotNull
    @Override
    protected Collection<DeclarationDescriptor> computeAllDescriptors() {
        Collection<DeclarationDescriptor> result = super.computeAllDescriptors();
        result.addAll(computeAllPackageDeclarations());
        return result;
    }

    @NotNull
    private Collection<DeclarationDescriptor> computeAllPackageDeclarations() {
        Collection<DeclarationDescriptor> result = Sets.newHashSet();

        for (PsiPackage psiSubPackage : psiPackage.getSubPackages()) {
            FqName fqName = new FqName(psiSubPackage.getQualifiedName());
            NamespaceDescriptor childNs = javaDescriptorResolver.resolveNamespace(fqName, IGNORE_KOTLIN_SOURCES);
            if (childNs != null) {
                result.add(childNs);
            }
        }

        for (PsiClass psiClass : psiClassFinder.findPsiClasses(psiPackage)) {
            if (DescriptorResolverUtils.isCompiledKotlinPackageClass(psiClass)) continue;

            if (psiClass instanceof JetJavaMirrorMarker) continue;

            if (!psiClass.hasModifierProperty(PsiModifier.PUBLIC)) continue;

            ProgressIndicatorProvider.checkCanceled();

            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName == null) continue;
            FqName fqName = new FqName(qualifiedName);

            ClassDescriptor classDescriptor = javaDescriptorResolver.resolveClass(fqName, IGNORE_KOTLIN_SOURCES);
            if (classDescriptor != null) {
                result.add(classDescriptor);
            }

            NamespaceDescriptor namespace = javaDescriptorResolver.resolveNamespace(fqName, IGNORE_KOTLIN_SOURCES);
            if (namespace != null) {
                result.add(namespace);
            }
        }

        return result;
    }

    @Override
    @NotNull
    protected Set<FunctionDescriptor> computeFunctionDescriptor(@NotNull Name name) {
        NamedMembers members = membersProvider.get(name);
        if (members == null) {
            return Collections.emptySet();
        }
        return javaDescriptorResolver.resolveFunctionGroupForPackage(members, (NamespaceDescriptor) descriptor);
    }

    @NotNull
    @Override
    protected Collection<ClassDescriptor> computeInnerClasses() {
        return Collections.emptyList();
    }
}
