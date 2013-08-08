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

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.DeclarationDescriptor;
import org.jetbrains.jet.lang.descriptors.NamespaceDescriptor;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.resolver.JavaMemberResolver;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.Collection;
import java.util.Collections;

public final class JavaClassStaticMembersScope extends JavaClassMembersScope {
    @NotNull
    private final FqName packageFQN;
    @NotNull
    private final JavaClass javaClass;

    public JavaClassStaticMembersScope(
            @NotNull NamespaceDescriptor descriptor,
            @NotNull FqName packageFQN,
            @NotNull JavaClass javaClass,
            @NotNull JavaMemberResolver memberResolver
    ) {
        super(descriptor, MembersProvider.forClass(javaClass, true), memberResolver);
        this.packageFQN = packageFQN;
        this.javaClass = javaClass;
    }

    @Override
    public NamespaceDescriptor getNamespace(@NotNull Name name) {
        return memberResolver.resolveNamespace(packageFQN.child(name), DescriptorSearchRule.INCLUDE_KOTLIN);
    }

    @NotNull
    @Override
    protected Collection<DeclarationDescriptor> computeAllDescriptors() {
        Collection<DeclarationDescriptor> result = super.computeAllDescriptors();
        for (JavaClass nested : javaClass.getInnerClasses()) {
            ContainerUtil.addIfNotNull(result, getNamespace(nested.getName()));
        }
        return result;
    }

    @NotNull
    @Override
    protected Collection<ClassDescriptor> computeInnerClasses() {
        return Collections.emptyList();
    }
}
