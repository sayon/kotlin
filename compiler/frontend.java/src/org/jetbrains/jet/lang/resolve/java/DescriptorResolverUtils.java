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

package org.jetbrains.jet.lang.resolve.java;

import com.google.common.collect.ImmutableSet;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiSubstitutor;
import com.intellij.psi.impl.compiled.ClsClassImpl;
import com.intellij.psi.util.PsiFormatUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.BindingTrace;
import org.jetbrains.jet.lang.resolve.OverrideResolver;
import org.jetbrains.jet.lang.resolve.java.resolver.ErrorReporter;
import org.jetbrains.jet.lang.resolve.java.structure.JavaClass;
import org.jetbrains.jet.lang.resolve.java.structure.JavaField;
import org.jetbrains.jet.lang.resolve.java.structure.JavaMember;
import org.jetbrains.jet.lang.resolve.java.structure.JavaMethod;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;

import java.util.*;

import static com.intellij.psi.util.PsiFormatUtilBase.*;
import static org.jetbrains.jet.lang.resolve.DescriptorUtils.isEnumClassObject;

public final class DescriptorResolverUtils {
    private static final ImmutableSet<String> OBJECT_METHODS = ImmutableSet.of("hashCode()", "equals(java.lang.Object)", "toString()");

    private DescriptorResolverUtils() {
    }

    public static boolean isCompiledKotlinPackageClass(@NotNull PsiClass psiClass) {
        if (psiClass instanceof ClsClassImpl) {
            JavaClass javaClass = new JavaClass(psiClass);
            FqName fqName = javaClass.getFqName();
            if (fqName != null && PackageClassUtils.isPackageClassFqName(fqName)) {
                return hasAnnotation(javaClass, JvmAnnotationNames.KOTLIN_PACKAGE.getFqName());
            }
        }
        return false;
    }

    public static boolean isCompiledKotlinClass(@NotNull PsiClass psiClass) {
        if (psiClass instanceof ClsClassImpl) {
            return hasAnnotation(new JavaClass(psiClass), JvmAnnotationNames.KOTLIN_CLASS.getFqName());
        }
        return false;
    }

    public static boolean hasAnnotation(@NotNull JavaClass javaClass, @NotNull FqName annotationFqName) {
        return javaClass.findAnnotation(annotationFqName.asString()) != null;
    }

    public static boolean isCompiledKotlinClassOrPackageClass(@NotNull PsiClass psiClass) {
        return isCompiledKotlinClass(psiClass) || isCompiledKotlinPackageClass(psiClass);
    }

    @NotNull
    public static <D extends CallableMemberDescriptor> Collection<D> resolveOverrides(
            @NotNull Name name,
            @NotNull Collection<D> membersFromSupertypes,
            @NotNull Collection<D> membersFromCurrent,
            @NotNull ClassDescriptor classDescriptor,
            @NotNull BindingTrace trace
    ) {
        final Set<D> result = new HashSet<D>();

        OverrideResolver.generateOverridesInFunctionGroup(
                name, membersFromSupertypes, membersFromCurrent, classDescriptor,
                new OverrideResolver.DescriptorSink() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public void addToScope(@NotNull CallableMemberDescriptor fakeOverride) {
                        result.add((D) fakeOverride);
                    }

                    @Override
                    public void conflict(@NotNull CallableMemberDescriptor fromSuper, @NotNull CallableMemberDescriptor fromCurrent) {
                        // nop
                    }
                }
        );

        OverrideResolver.resolveUnknownVisibilities(result, trace);

        return result;
    }

    @Nullable
    public static ValueParameterDescriptor getAnnotationParameterByName(@NotNull Name name, @NotNull ClassDescriptor annotationClass) {
        Collection<ConstructorDescriptor> constructors = annotationClass.getConstructors();
        assert constructors.size() == 1 : "Annotation class descriptor must have only one constructor";

        for (ValueParameterDescriptor parameter : constructors.iterator().next().getValueParameters()) {
            if (parameter.getName().equals(name)) {
                return parameter;
            }
        }

        return null;
    }

    /**
     * @return true if {@code member} is a static member of enum class, which is to be put into its class object (and not into the
     *         corresponding package). This applies to enum entries, values() and valueOf(String) methods
     */
    public static boolean shouldBeInEnumClassObject(@NotNull JavaMember member) {
        if (!member.getContainingClass().isEnum()) return false;

        if (member instanceof JavaField && ((JavaField) member).isEnumEntry()) return true;

        if (!(member instanceof JavaMethod)) return false;
        String signature = PsiFormatUtil.formatMethod(((JavaMethod) member).getPsi(), PsiSubstitutor.EMPTY, SHOW_NAME | SHOW_PARAMETERS,
                                                      SHOW_TYPE | SHOW_FQ_CLASS_NAMES);

        return "values()".equals(signature) ||
               "valueOf(java.lang.String)".equals(signature);
    }

    public static boolean isCorrectOwnerForEnumMember(@NotNull ClassOrNamespaceDescriptor ownerDescriptor, @NotNull JavaMember member) {
        return isEnumClassObject(ownerDescriptor) == shouldBeInEnumClassObject(member);
    }

    @NotNull
    public static ErrorReporter createPsiBasedErrorReporter(@NotNull final PsiClass psiClass, @NotNull final BindingTrace trace) {
        return new ErrorReporter() {
            @Override
            public void reportIncompatibleAbiVersion(int actualVersion) {
                AbiVersionUtil.reportIncompatibleAbiVersion(psiClass, actualVersion, trace);
            }
        };
    }

    public static boolean isObjectMethodInInterface(@NotNull JavaMember member) {
        return member.getContainingClass().isInterface() && member instanceof JavaMethod && isObjectMethod((JavaMethod) member);
    }

    public static boolean isObjectMethod(@NotNull JavaMethod method) {
        String formattedMethod = PsiFormatUtil.formatMethod(
                method.getPsi(), PsiSubstitutor.EMPTY, SHOW_NAME | SHOW_PARAMETERS, SHOW_TYPE | SHOW_FQ_CLASS_NAMES);
        return OBJECT_METHODS.contains(formattedMethod);
    }

    @NotNull
    public static Collection<JavaClass> filterDuplicateClasses(@NotNull Collection<JavaClass> classes) {
        Set<FqName> addedQualifiedNames = new HashSet<FqName>(classes.size());
        List<JavaClass> result = new ArrayList<JavaClass>(classes.size());

        for (JavaClass javaClass : classes) {
            FqName fqName = javaClass.getFqName();
            if (fqName != null && addedQualifiedNames.add(fqName)) {
                result.add(javaClass);
            }
        }

        return result;
    }
}
