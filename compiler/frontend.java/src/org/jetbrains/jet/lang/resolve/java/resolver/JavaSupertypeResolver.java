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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ClassKind;
import org.jetbrains.jet.lang.descriptors.TypeParameterDescriptor;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.JvmAbi;
import org.jetbrains.jet.lang.resolve.java.TypeUsage;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClassifier;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClassifierType;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.types.ErrorUtils;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.lang.types.TypeUtils;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class JavaSupertypeResolver {
    public static final FqName OBJECT_FQ_NAME = new FqName("java.lang.Object");

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

    @NotNull
    public Collection<JetType> getSupertypes(
            @NotNull ClassDescriptor classDescriptor,
            @NotNull JavaClass javaClass,
            @NotNull List<TypeParameterDescriptor> typeParameters
    ) {
        TypeVariableResolver typeVariableResolver = new TypeVariableResolver(typeParameters, classDescriptor,
                                                                                          "class " + javaClass.getFqName());

        List<JetType> result = transformSupertypeList(javaClass.getSupertypes(), typeVariableResolver);

        reportIncompleteHierarchyForErrorTypes(classDescriptor, result);

        if (result.isEmpty()) {
            return Collections.singletonList(getDefaultSupertype(javaClass));
        }

        return result;
    }

    @NotNull
    private JetType getDefaultSupertype(@NotNull JavaClass javaClass) {
        if (OBJECT_FQ_NAME.equals(javaClass.getFqName()) || javaClass.getKind() == ClassKind.ANNOTATION_CLASS) {
            return KotlinBuiltIns.getInstance().getAnyType();
        }
        else {
            ClassDescriptor object = classResolver.resolveClass(OBJECT_FQ_NAME, DescriptorSearchRule.IGNORE_IF_FOUND_IN_KOTLIN);
            if (object != null) {
                return object.getDefaultType();
            }
            else {
                //TODO: hack here
                return KotlinBuiltIns.getInstance().getAnyType();
                // throw new IllegalStateException("Could not resolve java.lang.Object");
            }
        }
    }

    private void reportIncompleteHierarchyForErrorTypes(@NotNull ClassDescriptor classDescriptor, @NotNull List<JetType> result) {
        for (JetType supertype : result) {
            if (ErrorUtils.isErrorType(supertype)) {
                trace.record(BindingContext.INCOMPLETE_HIERARCHY, classDescriptor);
            }
        }
    }

    @NotNull
    private List<JetType> transformSupertypeList(
            @NotNull Collection<JavaClassifierType> supertypes,
            @NotNull TypeVariableResolver typeVariableResolver
    ) {
        List<JetType> result = new ArrayList<JetType>(supertypes.size());
        for (JavaClassifierType type : supertypes) {
            JavaClassifier resolved = type.getClassifier();
            if (resolved != null) {
                assert resolved instanceof JavaClass : "Supertype should be a class: " + resolved;
                FqName fqName = ((JavaClass) resolved).getFqName();
                assert fqName != null : "Unresolved supertype: " + resolved;
                if (JvmAbi.JET_OBJECT.getFqName().equals(fqName)) {
                    continue;
                }
            }

            JetType transformed = typeTransformer.transformToType(type, TypeUsage.SUPERTYPE, typeVariableResolver);
            if (!ErrorUtils.isErrorType(transformed)) {
                result.add(TypeUtils.makeNotNullable(transformed));
            }
        }
        return result;
    }
}
