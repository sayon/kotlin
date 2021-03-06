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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ClassKind;
import org.jetbrains.jet.lang.descriptors.TypeParameterDescriptor;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.java.JavaTypeTransformer;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.TypeUsage;
import org.jetbrains.jet.lang.resolve.java.TypeVariableResolver;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeUtils;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule.IGNORE_KOTLIN_SOURCES;

public final class JavaSupertypeResolver {
    private static final FqName OBJECT_FQ_NAME = new FqName("java.lang.Object");

    private BindingTrace trace;
    private JavaTypeTransformer typeTransformer;
    private JavaClassResolver classResolver;

    @Inject
    public void setTrace(BindingTrace trace) {
        this.trace = trace;
    }

    @Inject
    public void setTypeTransformer(JavaTypeTransformer typeTransformer) {
        this.typeTransformer = typeTransformer;
    }

    @Inject
    public void setClassResolver(JavaClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    public Collection<JetType> getSupertypes(
            @NotNull ClassDescriptor classDescriptor,
            @NotNull PsiClass psiClass,
            @NotNull List<TypeParameterDescriptor> typeParameters
    ) {

        List<JetType> result = new ArrayList<JetType>();

        String context = "class " + psiClass.getQualifiedName();

        TypeVariableResolver typeVariableResolverForSupertypes = new TypeVariableResolver(typeParameters, classDescriptor, context);
        transformSupertypeList(result, psiClass.getExtendsListTypes(), typeVariableResolverForSupertypes);
        transformSupertypeList(result, psiClass.getImplementsListTypes(), typeVariableResolverForSupertypes);

        reportIncompleteHierarchyForErrorTypes(classDescriptor, result);

        if (result.isEmpty()) {
            addBaseClass(psiClass, classDescriptor, result);
        }
        return result;
    }

    private void addBaseClass(@NotNull PsiClass psiClass, @NotNull ClassDescriptor classDescriptor, @NotNull List<JetType> result) {
        if (OBJECT_FQ_NAME.asString().equals(psiClass.getQualifiedName()) || classDescriptor.getKind() == ClassKind.ANNOTATION_CLASS) {
            result.add(KotlinBuiltIns.getInstance().getAnyType());
        }
        else {
            ClassDescriptor object = classResolver.resolveClass(OBJECT_FQ_NAME, IGNORE_KOTLIN_SOURCES);
            if (object != null) {
                result.add(object.getDefaultType());
            }
            else {
                //TODO: hack here
                result.add(KotlinBuiltIns.getInstance().getAnyType());
                // throw new IllegalStateException("Could not resolve java.lang.Object");
            }
        }
    }

    private void reportIncompleteHierarchyForErrorTypes(ClassDescriptor classDescriptor, List<JetType> result) {
        for (JetType supertype : result) {
            if (ErrorUtils.isErrorType(supertype)) {
                trace.record(BindingContext.INCOMPLETE_HIERARCHY, classDescriptor);
            }
        }
    }

    private void transformSupertypeList(
            List<JetType> result,
            PsiClassType[] extendsListTypes,
            TypeVariableResolver typeVariableResolver
    ) {
        for (PsiClassType type : extendsListTypes) {
            PsiClass resolved = type.resolve();
            if (resolved != null) {
                String qualifiedName = resolved.getQualifiedName();
                assert qualifiedName != null;
                if (JvmAbi.JET_OBJECT.getFqName().equalsTo(qualifiedName)) {
                    continue;
                }
            }

            JetType transform = typeTransformer
                    .transformToType(type, TypeUsage.SUPERTYPE, typeVariableResolver);
            if (ErrorUtils.isErrorType(transform)) {
                continue;
            }

            result.add(TypeUtils.makeNotNullable(transform));
        }
    }
}
