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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.ClassDescriptor;
import org.jetbrains.jet.lang.descriptors.PropertyDescriptor;
import org.jetbrains.jet.lang.descriptors.ValueParameterDescriptor;
import org.jetbrains.jet.lang.descriptors.VariableDescriptor;
import org.jetbrains.jet.lang.descriptors.annotations.AnnotationDescriptor;
import org.jetbrains.jet.lang.resolve.constants.*;
import org.jetbrains.jet.lang.resolve.constants.StringValue;
import org.jetbrains.jet.lang.resolve.java.DescriptorResolverUtils;
import org.jetbrains.jet.lang.resolve.java.DescriptorSearchRule;
import org.jetbrains.jet.lang.resolve.java.structure.*;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class JavaCompileTimeConstResolver {
    private JavaAnnotationResolver annotationResolver;
    private JavaClassResolver classResolver;

    public JavaCompileTimeConstResolver() {
    }

    @Inject
    public void setAnnotationResolver(JavaAnnotationResolver annotationResolver) {
        this.annotationResolver = annotationResolver;
    }

    @Inject
    public void setClassResolver(JavaClassResolver classResolver) {
        this.classResolver = classResolver;
    }

    @Nullable
    public CompileTimeConstant<?> resolveAnnotationArgument(
            @NotNull FqName annotationFqName,
            @NotNull JavaAnnotationArgument argument,
            @NotNull PostponedTasks postponedTasks
    ) {
        if (argument instanceof JavaLiteralAnnotationArgument) {
            return resolveCompileTimeConstantValue(((JavaLiteralAnnotationArgument) argument).getValue());
        }
        // Enum
        else if (argument instanceof JavaReferenceAnnotationArgument) {
            return resolveFromReference(((JavaReferenceAnnotationArgument) argument).resolve(), postponedTasks);
        }
        // Array
        else if (argument instanceof JavaArrayAnnotationArgument) {
            Name argumentName = argument.getName();
            return resolveFromArray(
                    annotationFqName,
                    argumentName == null ? JavaAnnotationResolver.DEFAULT_ANNOTATION_MEMBER_NAME : argumentName,
                    ((JavaArrayAnnotationArgument) argument).getElements(),
                    postponedTasks
            );
        }
        // Annotation
        else if (argument instanceof JavaAnnotationAsAnnotationArgument) {
            return resolveFromAnnotation(((JavaAnnotationAsAnnotationArgument) argument).getAnnotation(), postponedTasks);
        }

        return null;
    }

    @Nullable
    private CompileTimeConstant<?> resolveFromAnnotation(@NotNull JavaAnnotation value, @NotNull PostponedTasks taskList) {
        AnnotationDescriptor descriptor = annotationResolver.resolveAnnotation(value, taskList);
        return descriptor == null ? null : new AnnotationValue(descriptor);
    }

    @Nullable
    private CompileTimeConstant<?> resolveFromArray(
            @NotNull FqName annotationFqName,
            @NotNull Name argumentName,
            @NotNull Collection<JavaAnnotationArgument> elements,
            @NotNull PostponedTasks taskList
    ) {
        ClassDescriptor annotationClass = classResolver.resolveClass(annotationFqName, DescriptorSearchRule.INCLUDE_KOTLIN, taskList);
        if (annotationClass == null) return null;

        //TODO: nullability issues
        ValueParameterDescriptor valueParameter = DescriptorResolverUtils.getAnnotationParameterByName(argumentName, annotationClass);
        if (valueParameter == null) return null;

        List<CompileTimeConstant<?>> values = new ArrayList<CompileTimeConstant<?>>(elements.size());
        for (JavaAnnotationArgument argument : elements) {
            CompileTimeConstant<?> value = resolveAnnotationArgument(annotationFqName, argument, taskList);
            values.add(value == null ? NullValue.NULL : value);
        }

        return new ArrayValue(values, valueParameter.getType());
    }

    @Nullable
    private CompileTimeConstant<?> resolveFromReference(@Nullable JavaElement element, @NotNull PostponedTasks taskList) {
        if (!(element instanceof JavaField)) return null;

        JavaField field = (JavaField) element;
        if (!field.isEnumEntry()) return null;

        JavaClass javaClass = field.getContainingClass();
        if (javaClass == null) return null;

        FqName fqName = javaClass.getFqName();
        if (fqName == null) return null;

        ClassDescriptor enumClass = classResolver.resolveClass(fqName, DescriptorSearchRule.INCLUDE_KOTLIN, taskList);
        if (enumClass == null) return null;

        ClassDescriptor enumClassObject = enumClass.getClassObjectDescriptor();
        if (enumClassObject == null) return null;

        JetScope scope = enumClassObject.getDefaultType().getMemberScope();
        for (VariableDescriptor variableDescriptor : scope.getProperties(field.getName())) {
            if (variableDescriptor.getReceiverParameter() == null) {
                return new EnumValue((PropertyDescriptor) variableDescriptor);
            }
        }

        return null;
    }

    @Nullable
    public static CompileTimeConstant<?> resolveCompileTimeConstantValue(@Nullable Object value) {
        if (value instanceof String) {
            return new StringValue((String) value);
        }
        else if (value instanceof Byte) {
            return new ByteValue((Byte) value);
        }
        else if (value instanceof Short) {
            return new ShortValue((Short) value);
        }
        else if (value instanceof Character) {
            return new CharValue((Character) value);
        }
        else if (value instanceof Integer) {
            return new IntValue((Integer) value);
        }
        else if (value instanceof Long) {
            return new LongValue((Long) value);
        }
        else if (value instanceof Float) {
            return new FloatValue((Float) value);
        }
        else if (value instanceof Double) {
            return new DoubleValue((Double) value);
        }
        else if (value instanceof Boolean) {
            return BooleanValue.valueOf((Boolean) value);
        }
        else if (value == null) {
            return NullValue.NULL;
        }
        return null;
    }
}