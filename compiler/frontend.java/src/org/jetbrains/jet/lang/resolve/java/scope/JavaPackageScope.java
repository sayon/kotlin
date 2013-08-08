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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.java.DescriptorResolverUtils;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.jetAsJava.JetJavaMirrorMarker;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaMemberResolver;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.java.structure.JavaPackage;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class JavaPackageScope extends JavaBaseScope {
    @NotNull
    private final JavaPackage javaPackage;
    @NotNull
    private final FqName packageFQN;

    public JavaPackageScope(
            @NotNull NamespaceDescriptor descriptor,
            @NotNull JavaPackage javaPackage,
            @NotNull FqName packageFQN,
            @NotNull JavaMemberResolver memberResolver
    ) {
        super(descriptor, memberResolver, MembersProvider.forPackage(javaPackage));
        this.javaPackage = javaPackage;
        this.packageFQN = packageFQN;
    }

    @Override
    public ClassifierDescriptor getClassifier(@NotNull Name name) {
        ClassDescriptor classDescriptor = memberResolver.resolveClass(packageFQN.child(name));
        if (classDescriptor == null || classDescriptor.getKind().isObject()) {
            return null;
        }
        return classDescriptor;
    }

    @Override
    public ClassDescriptor getObjectDescriptor(@NotNull Name name) {
        ClassDescriptor classDescriptor = memberResolver.resolveClass(packageFQN.child(name));
        if (classDescriptor != null && classDescriptor.getKind().isObject()) {
            return classDescriptor;
        }
        return null;
    }

    @Override
    public NamespaceDescriptor getNamespace(@NotNull Name name) {
        return memberResolver.resolveNamespace(packageFQN.child(name), DescriptorSearchRule.INCLUDE_KOTLIN);
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

        for (JavaPackage subPackage : javaPackage.getSubPackages()) {
            NamespaceDescriptor childNs =
                    memberResolver.resolveNamespace(subPackage.getFqName(), DescriptorSearchRule.IGNORE_IF_FOUND_IN_KOTLIN);
            if (childNs != null) {
                result.add(childNs);
            }
        }

        for (JavaClass javaClass : DescriptorResolverUtils.filterDuplicateClasses(javaPackage.getClasses())) {
            if (DescriptorResolverUtils.isCompiledKotlinPackageClass(javaClass.getPsi())) continue;

            if (javaClass.getPsi() instanceof JetJavaMirrorMarker) continue;

            if (javaClass.getVisibility() != Visibilities.PUBLIC) continue;

            ProgressIndicatorProvider.checkCanceled();

            FqName fqName = javaClass.getFqName();
            if (fqName == null) continue;

            ClassDescriptor classDescriptor = memberResolver.resolveClass(fqName);
            if (classDescriptor != null) {
                result.add(classDescriptor);
            }

            NamespaceDescriptor namespace = memberResolver.resolveNamespace(fqName, DescriptorSearchRule.IGNORE_IF_FOUND_IN_KOTLIN);
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
        return memberResolver.resolveFunctionGroupForPackage(members, (NamespaceDescriptor) descriptor);
    }

    @NotNull
    @Override
    protected Collection<ClassDescriptor> computeInnerClasses() {
        return Collections.emptyList();
    }
}
