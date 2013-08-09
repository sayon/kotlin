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

import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.resolve.constants.CompileTimeConstant;
import org.jetbrains.jet.lang.resolve.java.DescriptorResolverUtils;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.JvmAnnotationNames;
import org.jetbrains.jet.lang.resolve.java.JvmClassName;
import org.jetbrains.jet.lang.resolve.java.mapping.JavaToKotlinClassMap;
import org.jetbrains.jet.lang.resolve.java.structure.JavaAnnotation;
import org.jetbrains.jet.lang.resolve.java.structure.JavaAnnotationArgument;
import org.jetbrains.jet.lang.resolve.java.structure.JavaAnnotationOwner;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public final class JavaAnnotationResolver {
    public static final Name DEFAULT_ANNOTATION_MEMBER_NAME = Name.identifier("value");

    private JavaClassResolver classResolver;
    private JavaAnnotationArgumentResolver argumentResolver;
    private ExternalAnnotationResolver externalAnnotationResolver;

    public JavaAnnotationResolver() {
    }

    @Inject
    public void setClassResolver(JavaClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Inject
    public void setArgumentResolver(JavaAnnotationArgumentResolver argumentResolver) {
        this.argumentResolver = argumentResolver;
    }

    @Inject
    public void setExternalAnnotationResolver(ExternalAnnotationResolver externalAnnotationResolver) {
        this.externalAnnotationResolver = externalAnnotationResolver;
    }

    @NotNull
    public List<AnnotationDescriptor> resolveAnnotations(@NotNull JavaAnnotationOwner owner, @NotNull PostponedTasks tasks) {
        List<AnnotationDescriptor> result = new ArrayList<AnnotationDescriptor>();

        for (JavaAnnotation annotation : owner.getAnnotations()) {
            ContainerUtil.addIfNotNull(result, resolveAnnotation(annotation, tasks));
        }

        for (JavaAnnotation annotation : externalAnnotationResolver.findExternalAnnotations(owner)) {
            ContainerUtil.addIfNotNull(result, resolveAnnotation(annotation, tasks));
        }

        return result;
    }

    @NotNull
    public List<AnnotationDescriptor> resolveAnnotations(@NotNull JavaAnnotationOwner owner) {
        PostponedTasks postponedTasks = new PostponedTasks();
        List<AnnotationDescriptor> annotations = resolveAnnotations(owner, postponedTasks);
        postponedTasks.performTasks();
        return annotations;
    }

    @Nullable
    public AnnotationDescriptor resolveAnnotation(@NotNull JavaAnnotation javaAnnotation, @NotNull PostponedTasks postponedTasks) {
        final AnnotationDescriptor annotation = new AnnotationDescriptor();
        FqName fqName = javaAnnotation.getFqName();
        if (fqName == null) {
            return null;
        }

        // Don't process internal jet annotations and jetbrains NotNull annotations
        if (fqName.asString().startsWith("jet.runtime.typeinfo.")
            || fqName.equals(JvmAnnotationNames.JETBRAINS_NOT_NULL_ANNOTATION.getFqName())
            || fqName.equals(JvmAnnotationNames.KOTLIN_CLASS.getFqName())
            || fqName.equals(JvmAnnotationNames.KOTLIN_PACKAGE.getFqName())
        ) {
            return null;
        }

        AnnotationDescriptor mappedClassDescriptor = JavaToKotlinClassMap.getInstance().mapToAnnotationClass(fqName);
        if (mappedClassDescriptor != null) {
            return mappedClassDescriptor;
        }

        final ClassDescriptor annotationClass = classResolver.resolveClass(fqName, DescriptorSearchRule.INCLUDE_KOTLIN, postponedTasks);
        if (annotationClass == null) {
            return null;
        }

        postponedTasks.addTask(new Runnable() {
            @Override
            public void run() {
                annotation.setAnnotationType(annotationClass.getDefaultType());
            }
        });


        for (JavaAnnotationArgument argument : javaAnnotation.getArguments()) {
            if (argument == null) return null;

            CompileTimeConstant value = argumentResolver.resolveAnnotationArgument(fqName, argument, postponedTasks);
            if (value == null) continue;

            Name name = argument.getName();
            ValueParameterDescriptor descriptor = DescriptorResolverUtils.getAnnotationParameterByName(
                    name == null ? DEFAULT_ANNOTATION_MEMBER_NAME : name, annotationClass);
            if (descriptor != null) {
                annotation.setValueArgument(descriptor, value);
            }
        }

        return annotation;
    }

    @Nullable
    public JavaAnnotation findAnnotationWithExternal(@NotNull JavaAnnotationOwner owner, @NotNull JvmClassName name) {
        JavaAnnotation annotation = owner.findAnnotation(name.getFqName());
        if (annotation != null) {
            return annotation;
        }

        return externalAnnotationResolver.findExternalAnnotation(owner, name.getFqName());
    }

    public boolean hasNotNullAnnotation(@NotNull JavaAnnotationOwner owner) {
        return findAnnotationWithExternal(owner, JvmAnnotationNames.JETBRAINS_NOT_NULL_ANNOTATION) != null;
    }

    public boolean hasMutableAnnotation(@NotNull JavaAnnotationOwner owner) {
        return findAnnotationWithExternal(owner, JvmAnnotationNames.JETBRAINS_MUTABLE_ANNOTATION) != null;
    }

    public boolean hasReadonlyAnnotation(@NotNull JavaAnnotationOwner owner) {
        return findAnnotationWithExternal(owner, JvmAnnotationNames.JETBRAINS_READONLY_ANNOTATION) != null;
    }
}